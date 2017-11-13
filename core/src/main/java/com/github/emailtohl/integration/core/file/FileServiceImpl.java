package com.github.emailtohl.integration.core.file;

import static com.github.emailtohl.integration.common.ConstantPattern.SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.lucene.FileSearch;
import com.github.emailtohl.integration.common.ztree.FileNode;
import com.github.emailtohl.integration.core.ExecResult;

/**
 * 管理文件的服务
 * @author HeLei
 */
@Service
public class FileServiceImpl implements FileService {
	private static final Logger LOG = LogManager.getLogger();
	final Set<String> charsets = Charset.availableCharsets().keySet();	@Inject
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
		// 对该目录创建索引
		fileSearch.index(resources);
		// 匹配Windows的“resources\”和UNIX的“resources/”
		root_pattern = Pattern.compile(resources.getName() + SEPARATOR);
	}

	@Override
	public boolean exist(String filename) {
		if (!StringUtils.hasText(filename)) {
			return false;
		}
		return new File(resources, filterPath(filename)).exists();
	}

	@Override
	public File getFile(String filename) {
		if (!StringUtils.hasText(filename)) {
			return new File(resources.getPath());// 根路径的副本
		}
		return new File(resources, filterPath(filename));
	}
	
	@Override
	public ExecResult createDir(String dirname) {
		if (!StringUtils.hasText(dirname)) {
			return new ExecResult(false, "path is empty", null);
		}
		String relativeDir = filterPath(dirname);
		File f = new File(resources, relativeDir);
		if (!f.exists()) {
			f.mkdirs();
		}
		return new ExecResult(true, null, relativeDir);
	}
	
	@Override
	public ExecResult reName(String srcName, String destName) {
		if (!StringUtils.hasText(srcName) || !StringUtils.hasText(destName)) {
			return new ExecResult(false, "name is empty", null);
		}
		File src = new File(resources, filterPath(srcName));
		if (!src.exists()) {
			return new ExecResult(false, "srcName is not exist", null);
		}
		File dest = new File(resources, filterPath(destName));
		if (dest.exists()) {
			return new ExecResult(false, "destName already exists", null);
		}
		if (dest.getParent() == null && src.getParent() != null) {
			return new ExecResult(false, "not in the same directory", null);
		}
		if (!dest.getParent().equals(src.getParent())) {
			return new ExecResult(false, "not in the same directory", null);
		}
		boolean b = src.renameTo(dest);
		if (b) {
			try {
				fileSearch.deleteIndex(src);
				fileSearch.addIndex(dest);
			} catch (IOException e) {
				LOG.catching(e);
				return new ExecResult(false, "reName index exception", null);
			}
		}
		return new ExecResult(true, null, null);
	}

	@Override
	public ExecResult delete(String filename) {
		if (!StringUtils.hasText(filename)) {
			return new ExecResult(false, "filename is empty", null);
		}
		File f = new File(resources, filterPath(filename));
		boolean b = FileUtils.deleteQuietly(f);
		if (b) {
			try {
				fileSearch.deleteIndex(f);
			} catch (IOException e) {
				LOG.catching(e);
				return new ExecResult(false, "remove index exception", null);
			}
		}
		return new ExecResult(b, null, null);
	}

	@Override
	public ExecResult save(String filename, InputStream in) {
		if (!StringUtils.hasText(filename)) {
			return new ExecResult(false, "filename is empty", null);
		}
		try {
			File f = new File(resources, filterPath(filename));
			boolean exist = f.exists();
			FileUtils.copyToFile(in, f);
			if (exist) {
				fileSearch.addIndex(f);
			} else {
				fileSearch.updateIndex(f);
			}
		} catch (IOException e) {
			LOG.catching(e);
			return new ExecResult(false, "io exception", null);
		}
		return new ExecResult(true, null, null);
	}

	@Override
	public Set<FileNode> findFile(String query) {
		FileNode root = FileNode.newInstance(resources);
		if (StringUtils.hasText(query)) {
			fileSearch.queryForFilePath(query).forEach(s -> {
				// s是全路径，如：F:\workspace\integration\core
				// integration是root，正则式是：integration[\\/]
				Matcher m = root_pattern.matcher(s);
				if (m.find()) {
					// path是从root路径开始，如integration\core
					String path = s.substring(m.start());
					root.setOpen(path);
				}
			});
		}
		return root.getChildren().stream().map(ztreeNode -> (FileNode) ztreeNode).collect(Collectors.toSet());
	}
	
	@Override
	public void reIndex() {
		try {
			fileSearch.index(resources);
		} catch (IOException e) {
			LOG.catching(e);
		}
	}

	@Override
	public Set<String> availableCharsets() {
		return charsets;
	}

	@Override
	public ExecResult loadText(String filename, String charset) {
		if (!StringUtils.hasText(filename)) {
			return new ExecResult(false, "filename is empty", null);
		}
		File f = new File(resources, filterPath(filename));
		try {
			String txt = FileUtils.readFileToString(f, charset);
			return new ExecResult(true, null, txt);
		} catch (IOException e) {
			LOG.catching(e);
			return new ExecResult(false, e.getMessage(), null);
		}
	}

	@Override
	public ExecResult writeText(String filename, String textContext, String charset) {
		if (!StringUtils.hasText(filename)) {
			return new ExecResult(false, "filename is empty", null);
		}
		File f = new File(resources, filterPath(filename));
		try {
			boolean exist = f.exists();
			FileUtils.write(f, textContext, charset);
			if (exist) {
				fileSearch.addIndex(f);
			} else {
				fileSearch.updateIndex(f);
			}
			return new ExecResult(true, null, null);
		} catch (IOException e) {
			LOG.catching(e);
			return new ExecResult(false, e.getMessage(), null);
		}
	}
	
	private String filterPath(String path) {
		StringBuilder s = new StringBuilder();
		boolean first = true;
		for (String dir : path.split(SEPARATOR)) {
			if (first) {
				s.append(dir);
				first = false;
			} else {
				s.append(File.separator).append(dir);
			}
		}
		return s.toString();
	}

}
