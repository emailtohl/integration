package com.github.emailtohl.integration.core.file;

import static com.github.emailtohl.integration.common.ConstantPattern.SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.lucene.FileSearch;
import com.github.emailtohl.integration.common.utils.TreeUtil;
import com.github.emailtohl.integration.common.utils.ZtreeNode;
import com.github.emailtohl.integration.core.ExecResult;

/**
 * 管理文件的服务
 * @author HeLei
 */
@Service
public class FileServiceImpl implements FileService {
	private static final Logger LOG = LogManager.getLogger();
	final Set<String> charsets = Charset.availableCharsets().keySet();
	@Inject
	@Named("resources")
	File resources;
	/**
	 * 自动存储的文件空间
	 */
	File autoSaveSpace;
	
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
		
		autoSaveSpace = new File(resources, "autoSaveSpace");
		if (!autoSaveSpace.exists()) {
			autoSaveSpace.mkdir();
		}
	}

	@Override
	public boolean exist(String pathname) {
		if (!StringUtils.hasText(pathname)) {
			return false;
		}
		return new File(resources, filterPath(pathname)).exists();
	}

	@Override
	public File getFile(String pathname) {
		if (!StringUtils.hasText(pathname)) {
			return new File(resources.getPath());// 根路径的副本
		}
		return new File(resources, filterPath(pathname));
	}
	
	/**
	 * 将基于resources的File，转成相对于resources的路径
	 */
	@Override
	public String getPath(File f) {
		if (f == null) {
			return "";
		}
		Matcher m = root_pattern.matcher(f.getPath());
		if (!m.find()) {
			return "";
		}
		return f.getPath().substring(m.end());
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
	public ExecResult delete(String pathname) {
		if (!StringUtils.hasText(pathname)) {
			return new ExecResult(false, "filename is empty", null);
		}
		File f = new File(resources, filterPath(pathname));
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
	public ExecResult save(String pathname, InputStream in) {
		if (!StringUtils.hasText(pathname)) {
			return new ExecResult(false, "filename is empty", null);
		}
		try {
			File f = new File(resources, filterPath(pathname));
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
	
	/**
	 * 根据内部存储情况自动存储文件，适用于图片等资料
	 * @param in 文件输入流
	 * @param suffix 文件后缀，若为null，则不保存文件后缀
	 * @return ExecResult.attribute中存储返回存储后的文件的路径+文件名，文件名由自动计算
	 */
	@Override
	public ExecResult autoSaveFile(InputStream in, String suffix) {
		LocalDate d = LocalDate.now();
		int year = d.getYear(), month = d.getMonthValue(), day = d.getDayOfMonth();
		File fyear = new File(autoSaveSpace, String.valueOf(year));
		if (!fyear.exists()) {
			fyear.mkdir();
		}
		File fmonth = new File(fyear, String.valueOf(month));
		if (!fmonth.exists()) {
			fmonth.mkdir();
		}
		File fday = new File(fmonth, String.valueOf(day));
		if (!fday.exists()) {
			fday.mkdir();
		}
		String filename = getSerialByUUId();
		if (StringUtils.hasText(suffix)) {
			filename = filename + '.' + suffix;
		}
		try {
			File f = new File(fday, filterPath(filename));
			FileUtils.copyToFile(in, f);
			return new ExecResult(true, null, getPath(f));
		} catch (IOException e) {
			LOG.catching(e);
			return new ExecResult(false, "io exception", null);
		}
	}
	
	@Override
	public List<ZtreeNode> findFile(String query) {
		List<ZtreeNode> nodes = TreeUtil.getZtreeNodeByFilesystem(Arrays.asList(resources.listFiles()));
		if (StringUtils.hasText(query)) {
			fileSearch.queryForFilePath(query).forEach(s -> {
				// fileSearch查出来的s是调用File的getCanonicalPath方法获取到的全路径
				Matcher m = root_pattern.matcher(s);
				if (m.find()) {
					String path = s.substring(m.end());
					TreeUtil.setOpen(nodes, Arrays.asList(path.split(ConstantPattern.SEPARATOR)));
				}
			});
		}
		return nodes;
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
	public ExecResult loadText(String pathname, String charset) {
		if (!StringUtils.hasText(pathname)) {
			return new ExecResult(false, "pathname is empty", null);
		}
		File f = new File(resources, filterPath(pathname));
		try {
			String txt = FileUtils.readFileToString(f, charset);
			return new ExecResult(true, null, txt);
		} catch (IOException e) {
			LOG.catching(e);
			return new ExecResult(false, e.getMessage(), null);
		}
	}

	@Override
	public ExecResult writeText(String pathname, String textContext, String charset) {
		if (!StringUtils.hasText(pathname)) {
			return new ExecResult(false, "pathname is empty", null);
		}
		File f = new File(resources, filterPath(pathname));
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

	public String getSerialByUUId() {
		int machineId = 1;// 最大支持1-9个集群机器部署
		int hashCodeV = UUID.randomUUID().toString().hashCode();
		if (hashCodeV < 0) {// 有可能是负数
			hashCodeV = -hashCodeV;
		}
		// 0 代表前面补充0
		// 4 代表长度为4
		// d 代表参数为正数型
		return machineId + String.format("%04d", hashCodeV);
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
