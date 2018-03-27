package com.github.emailtohl.integration.common.lucene;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.utils.TextUtil;

/**
 * lucene的数据源获取有很多开源框架，如Solr提取数据库和XML；Nutch、Heritrix、Grub获取web站点；
 * Aperture支持web站点，文件系统、邮箱等；Tika提供数据过滤。
 * 
 * 本类同时集成了IndexWriter和IndexReader，但并未对增删改的并发做太多限制，这是因为Lucene高度支持索引的并发访问。
 * 多个Reader可以共享同一索引，多个线程可以共享同一IndexWriter或IndexReader。
 * 对于增删改的并发的唯一限制就是不能同时打开多于一个writer，如果实例化相同索引目录的writer就会遇到LockObtainFailedException。
 * 
 * 不过本类对更新索引做了并发限制，主要是因为更新索引时，需要关闭原有的IndexReader，若此时有查询线程在执行，就会出现问题。
 * 本来lucene提供ReferenceManager<IndexSearcher> referenceManager = new SearcherManager(indexWriter, true, new SearcherFactory());
 * 支持并发场景下的IndexSearcher的更新，但在关闭资源的API上，我暂时还不清楚使用场景，故自实现锁机制。
 * 
 * 注意：对于文本文件，目前只支持UTF-8格式。
 * 
 * 本工具只是适应本项目中轻量级的对文件系统建立索引，查询文本内容，更多应用还需借助成熟的开源框架。
 * 
 * @author HeLei
 */
public class FileSearch implements AutoCloseable {
	private static final Logger logger = LogManager.getLogger();
	public static final String FILE_NAME = "fileName";
	public static final String FILE_TIME = "fileTime";
	public static final String FILE_CONTENT = "fileContent";
	public static final String FILE_PATH = "filePath";
	public static final String FILE_SIZE = "fileSize";
	public static final int TOP_HITS = 1000;
	/** 是否索引过，如果已经索引了，则不能再设置分词器 */
	private volatile boolean isIndexed = false;
	/** 分词器 */
	private Analyzer analyzer = new StandardAnalyzer();
	/** 索引库 */
	private final Directory indexBase;
	/** 索引写入器 */
	private IndexWriter indexWriter;
	/** 索引阅读器 */
	private IndexReader indexReader;
	/** 索引搜索器 */
	private IndexSearcher indexSearcher;
	/** 文本文件过滤器 */
	private FileFilter textFileFilter = new TextFilesFilter();
	/** 查询线程的计数器，没有查询时为0，这时候可以更新IndexReader */
	private volatile int queryCount = 0;

	/**
	 * 可接受文件系统的索引目录，也可以接受内存形式的索引目录
	 * 
	 * @param indexBase 索引目录
	 */
	public FileSearch(Directory indexBase) {
		this.indexBase = indexBase;
	}

	/**
	 * 只接受文件系统的索引目录
	 * 
	 * @param indexBaseFSDirectory 文件系统的索引目录
	 * @throws IOException
	 */
	public FileSearch(String indexBaseFSDirectory) throws IOException {
		this.indexBase = FSDirectory.open(Paths.get(indexBaseFSDirectory));
	}

	/**
	 * 为需要查询的目录创建索引
	 * 
	 * @param searchDir 需要查询的目录
	 * @return 被索引的Document数
	 * @throws IOException
	 */
	public synchronized int index(File searchDir) throws IOException {
		_close();
		int numIndexed = 0;
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		// 每一次都会进行创建新的索引,第二次删掉原来的创建新的索引
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		// 创建索引的indexWriter
		indexWriter = new IndexWriter(indexBase, indexWriterConfig);
		// 采集原始文档
		appendDocument(searchDir, indexWriter);
		indexWriter.commit();
		numIndexed = indexWriter.numDocs();
		isIndexed = true;
		indexReader = DirectoryReader.open(indexWriter);
		indexSearcher = new IndexSearcher(indexReader);
		return numIndexed;
	}
	
	/**
	 * 将文本文件读为lucene的Document并添加进IndexWriter
	 * 
	 * @param file
	 * @param indexWriter
	 * @throws IOException
	 */
	private void appendDocument(File file, IndexWriter indexWriter) throws IOException {
		if (textFileFilter.accept(file)) {
			indexWriter.addDocument(getDocument(file));
		} else if (file.isDirectory()) {
			for (File sub : file.listFiles()) {
				appendDocument(sub, indexWriter);
			}
		}
	}

	/**
	 * 查询出Lucene原始的Document对象
	 * 
	 * @param queryString
	 * @return
	 */
	public List<Document> query(String queryString) {
		List<Document> list = new ArrayList<Document>();
		try {
			synchronized (this) {
				while (queryCount < 0)
					wait();
				queryCount++;
			}
			String[] fields = { FILE_NAME, FILE_TIME, FILE_CONTENT, FILE_PATH, FILE_SIZE };
			QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
			// Query q = new TermQuery(new Term(FILE_CONTENT, queryString));
			Query query = queryParser.parse(queryString);
			TopDocs docs = indexSearcher.search(query, TOP_HITS);
			logger.debug(docs.totalHits);
			for (ScoreDoc sd : docs.scoreDocs) {
				logger.debug(sd.score);
				Document doc = indexSearcher.doc(sd.doc);
				logger.debug(doc);
				list.add(doc);
			}
		} catch (IOException e) {
			logger.error("打开索引库失败", e);
		} catch (ParseException e) {
			logger.error("查询语句解析失败", e);
		} catch (InterruptedException e) {
			logger.catching(e);
		} finally {
			synchronized (this) {
				queryCount--;
				notifyAll();
			}
		}
		return list;
	}

	/**
	 * 分页查询出Lucene原始的Document对象
	 * 
	 * @param queryString 查询语句
	 * @param pageable Spring-data的分页对象
	 * @return Spring-data的页面对象
	 */
	public Page<Document> query(String queryString, Pageable pageable) {
		List<Document> list = new ArrayList<Document>();
		int count = 0;
		try {
			synchronized (this) {
				while (queryCount < 0)
					wait();
				queryCount++;
			}
			String[] fields = { FILE_NAME, FILE_TIME, FILE_CONTENT, FILE_PATH, FILE_SIZE };
			QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
			Query query = queryParser.parse(queryString);
			count = indexSearcher.count(query);
			Sort sort = getSort(pageable);
			TopDocs docs = indexSearcher.search(query, TOP_HITS, sort);
			logger.debug(docs.totalHits);
			int offset = pageable.getOffset();
			int end = offset + pageable.getPageSize();

			for (int i = offset; i < end && i < count && i < TOP_HITS; i++) {
				ScoreDoc sd = docs.scoreDocs[i];
				logger.debug(sd.score);
				Document doc = indexSearcher.doc(sd.doc);
				logger.debug(doc);
				list.add(doc);
			}
		} catch (IOException e) {
			logger.error("打开索引库失败", e);
		} catch (ParseException e) {
			logger.error("查询语句解析失败", e);
		} catch (InterruptedException e) {
			logger.catching(e);
		} finally {
			synchronized (this) {
				queryCount--;
				notifyAll();
			}
		}
		return new PageImpl<Document>(list, pageable, count);
	}

	private Sort getSort(Pageable pageable) {
		Sort sort = new Sort();
		List<SortField> ls = new ArrayList<SortField>();
		org.springframework.data.domain.Sort s = pageable.getSort();
		if (s != null) {
			for (Iterator<org.springframework.data.domain.Sort.Order> i = s.iterator(); i.hasNext();) {
				org.springframework.data.domain.Sort.Order o = i.next();
				SortField sortField = new SortField(o.getProperty(), Type.SCORE);// 以相关度进行排序
				ls.add(sortField);
			}
		}
		if (ls.size() > 0) {
			SortField[] sortFields = new SortField[ls.size()];
			sort.setSort(ls.toArray(sortFields));
		}
		return sort;
	}

	/**
	 * 添加文件的索引
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void addIndex(File file) throws IOException {
		if (!textFileFilter.accept(file)) {
			return;
		}
		Document doc = getDocument(file);
		indexWriter.addDocument(doc);
		indexWriter.commit();
		refreshIndexReader();
		isIndexed = true;
	}

	/**
	 * 更新文件的索引
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void updateIndex(File file) throws IOException {
		if (!textFileFilter.accept(file)) {
			return;
		}
		Document doc = getDocument(file);
		indexWriter.updateDocument(new Term(FILE_PATH, file.getPath()), doc);
		indexWriter.commit();
		refreshIndexReader();
		isIndexed = true;
	}

	/**
	 * 删除文件的索引
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void deleteIndex(File file) throws IOException {
		if (textFileFilter.accept(file)) {
			indexWriter.deleteDocuments(new Term(FILE_PATH, file.getPath()));
			indexWriter.commit();
		}
		refreshIndexReader();
	}

	/**
	 * 将查询结果以文件的路径返回
	 * 
	 * @param query
	 * @return 返回的路径是相对于index时的路径，若index时是绝对路径，则返回的也是绝对路径
	 */
	public Set<String> queryForFilePath(String queryString) {
		Set<String> paths = new TreeSet<String>();
		List<Document> list = query(queryString);
		for (Document doc : list) {
			paths.add(doc.getField(FILE_PATH).stringValue());
		}
		return paths;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public synchronized void setAnalyzer(Analyzer analyzer) {
		if (isIndexed)
			throw new IllegalStateException("已经被索引过，不能再设置分词器!");
		this.analyzer = analyzer;
	}

	/**
	 * 分析文本文件，并创建一个Lucene的Document
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private Document getDocument(File file) throws IOException {
		// String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		FileInputStream fis = null;
		String content = "";
		try {
			fis = new FileInputStream(file);
			content = TextUtil.readFileToString(fis);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		// TextField既被索引又被分词，但是没有词向量
		Field fName = new TextField(FILE_NAME, file.getName(), Store.YES);
		fName.setBoost(1.2F);
		Field fContent = new TextField(FILE_CONTENT, content, Store.NO);
		// StringField被索引不被分词，整个值被看作为一个单独的token而被索引
		// 这里使用file.getCanonicalPath()，获取的是文件绝对路径，getCanonicalPath和getAbsolutePath的不同在于：
		//	File file = new File("..\\src\\test1.txt");
		//	System.out.println(file.getAbsolutePath());
		//	D:\workspace\test\..\src\test1.txt
		//	System.out.println(file.getCanonicalPath());
		//	D:\workspace\src\test1.txt
		Field fPath = new StringField(FILE_PATH, file.getCanonicalPath(), Store.YES);
		Field fTime = new LongField(FILE_TIME, System.currentTimeMillis(), Store.YES);
		// 创建文档对象
		Document doc = new Document();
		doc.add(fName);
		doc.add(fContent);
		doc.add(fPath);
		doc.add(fTime);
		return doc;
	}
	
	/**
	 * 当索引变更时，为保持查询有效，需更新IndexReader
	 * @throws IOException
	 */
	private void refreshIndexReader() throws IOException {
		synchronized (this) {
			try {
				while (queryCount != 0)
					wait();
				queryCount--;
				IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) indexReader);
				if (newReader != null && newReader != indexReader) {
					indexReader.close();
					indexReader = newReader;
					indexSearcher = new IndexSearcher(indexReader);
				}
			} catch (InterruptedException e) {
				logger.catching(e);
			} finally {
				queryCount++;
				notifyAll();
			}
		}
	}

	@Override
	public void close() throws Exception {
		_close();
		isIndexed = false;
	}
	
	/**
	 * 关闭除indexBase的所有资源
	 * @throws IOException 
	 */
	private void _close() throws IOException  {
		// reader建立在writer上，先关闭reader，再关闭writer
		if (indexReader != null)
			indexReader.close();
		if (indexWriter != null && indexWriter.isOpen())
			indexWriter.close();
		// 整体关闭时，关闭掉indexBase，实际上isIndexed也没用了，因为indexBase关闭后，就不能再建索引
		isIndexed = false;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	/**
	 * 根据后缀过滤一部分不是文本的文件
	 * @author HeLei
	 */
	class TextFilesFilter implements FileFilter {
		private static final long MAX_BYTES = 10_485_760L;// 10兆
		private final Set<String> SUFFIX_SET = new HashSet<String>(Arrays.asList("dll", "jpg", "png", "gif", "tif",
				"bmp", "dwg", "psd", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "mdb", "wpd", "zip", "gz", "rar",
				"wav", "avi", "ram", "rm", "mpg", "mpq", "mov", "asf", "mid", "exe", "wps"));

		@Override
		public boolean accept(File f) {
			if (f.isDirectory() || f.length() > MAX_BYTES)
				return false;
			String ext = FilenameUtils.getExtension(f.getName());
			return !SUFFIX_SET.contains(ext.toLowerCase());
		}
	}

}
