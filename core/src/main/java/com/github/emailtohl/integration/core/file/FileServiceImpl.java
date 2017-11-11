package com.github.emailtohl.integration.core.file;

import static com.github.emailtohl.integration.common.ConstantPattern.SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.lucene.FileSearch;
import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.common.utils.TextUtil;
import com.github.emailtohl.integration.common.utils.UpDownloader;
import com.github.emailtohl.integration.common.ztree.FileNode;
import com.github.emailtohl.integration.common.ztree.ZtreeNode;

/**
 * 管理文件的服务
 * @author HeLei
 */
@Service
public class FileServiceImpl implements FileService {
	private static final Logger logger = LogManager.getLogger();
	private UpDownloader upDownloader;
	private TextUtil textUtil = new TextUtil();
	@Inject
	@Named("resources")
	File resources;
	/**
	 * FileSearch用到两个文件路径，一个是被搜索的资源目录，另一个是Lucene的索引目录
	 */
	@Inject
	FileSearch fileSearch;
	private Pattern root_pattern;
	
	@PostConstruct
	public void init() throws IOException {
		// 以此为根目录
		upDownloader = new UpDownloader(resources);
		// 对该目录创建索引
		fileSearch.index(resources);
		// 匹配Windows的“\resources\”和UNIX的“/resources/”
		root_pattern = Pattern.compile(SEPARATOR + resources.getName() + SEPARATOR);
	}

	@Override
	public Set<FileNode> findFile(String query) {
		/*FileNode node = FileNode.newInstance(resources);
		Set<ZtreeNode<File>> set = node.getChildren();
		if (StringUtils.hasText(query)) {
			fileSearch.queryForFilePath(query).forEach(s -> {
				Matcher m = root_pattern.matcher(s);
				if (m.find()) {
					String relativelyPath = s.substring(s.indexOf(m.end()));
					node.setOpen(relativelyPath);
				}
			});
		}*/
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		File f = new File(".");
		String path = f.getCanonicalPath();
		System.out.println(path);
		Pattern root_pattern = Pattern.compile(SEPARATOR + "integration" + SEPARATOR);
		System.out.println(root_pattern);
		Matcher m = root_pattern.matcher(path);
		if (m.find()) {
			int i = m.start();
			String relativelyPath = path.substring(i);
			System.out.println(relativelyPath);
		}
	
	}
	
	@Override
	public ExecResult createDir(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExecResult reName(String srcName, String destName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExecResult delete(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExecResult save(String path, InputStream in) {
		// TODO Auto-generated method stub
		return null;
	}

}
