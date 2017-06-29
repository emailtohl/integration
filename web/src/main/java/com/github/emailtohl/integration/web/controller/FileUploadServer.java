package com.github.emailtohl.integration.web.controller;

import static com.github.emailtohl.integration.common.Constant.PATTERN_SEPARATOR;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.emailtohl.integration.common.lucene.FileSearch;
import com.github.emailtohl.integration.common.utils.TextUtil;
import com.github.emailtohl.integration.common.utils.UpDownloader;
import com.github.emailtohl.integration.common.ztree.ZtreeNode;
import com.github.emailtohl.integration.web.exception.BadRequestException;
/**
 * 文件上传控制器
 * 本类涉及很多文件路径，为避免参数中的路径和实际路径冲突，所以请求统一为POST
 * @author HeLei
 * @date 2017.02.04
 */
@Controller
@RequestMapping("fileUploadServer")
public class FileUploadServer {
	private static final Logger logger = LogManager.getLogger();
	public static final String CMS_DIR = "cms_dir";
	private File cmsRoot;
	private Pattern cmsRoot_pattern;
	private TextUtil textUtil = new TextUtil();
	
	private Object textUpdateMutex = new Object();
	private Object fileMutex = new Object();
	
	private UpDownloader upDownloader;
	@Inject @Named("resources") File resources;
	/**
	 * FileSearch用到两个文件路径，一个是被搜索的资源目录，另一个是Lucene的索引目录
	 */
	@Inject FileSearch fileSearch;
	
	@PostConstruct
	public void init() throws IOException {
		cmsRoot = new File(resources, CMS_DIR);
		if (!cmsRoot.exists()) {
			cmsRoot.mkdir();
		}
		upDownloader = new UpDownloader(cmsRoot);
		// 对cms目录下创建索引
		fileSearch.index(cmsRoot);
		// 正则式，匹配CMS_DIR目录，用于判断是否cms目录
		cmsRoot_pattern = Pattern.compile("(^" + PATTERN_SEPARATOR + cmsRoot.getName() + PATTERN_SEPARATOR + "?)|(^cms_dir" + PATTERN_SEPARATOR + "?)");
		logger.debug(cmsRoot_pattern.matcher(cmsRoot.getName()));
	}
	
	@PreDestroy
	public void closeFileSearch() throws Exception {
		fileSearch.close();
	}
	
	/**
	 * 获取资源管理的根目录的数据结构
	 * @return
	 */
	@RequestMapping(value = "root", method = RequestMethod.GET)
	@ResponseBody
	public ZtreeNode getRoot() {
		return ZtreeNode.newInstance(cmsRoot);
	}

	/**
	 * 查询文本内容
	 * @param param 内容的字符串
	 * @return 路径集合
	 */
	@RequestMapping(value = "query", method = RequestMethod.GET)
	@ResponseBody
	public ZtreeNode query(@RequestParam(required = false, name = "param", defaultValue = "") String param) {
		ZtreeNode node = ZtreeNode.newInstance(cmsRoot);
		if (!param.isEmpty()) {
			fileSearch.queryForFilePath(param).forEach(s -> {
				// 传给前端的是相对于CMS_DIR的路径
				String relativelyPath = s.substring(s.indexOf(CMS_DIR));
				node.setOpen(relativelyPath);
			});
		}
		return node;
	}
	
	/**
	 * 创建一个目录
	 * @param dirName 目录相对路径
	 */
	@RequestMapping(value = "createDir", method = POST, produces = "text/plain; charset=utf-8")
	@ResponseBody
	public void createDir(String dirName) {
		String relativePath = getRelativePath(dirName);
		File f = new File(upDownloader.getAbsolutePath(relativePath));
		if (!f.exists()) {
			f.mkdirs();
		}
	}
	
	/**
	 * 为目录或文件改名
	 * @param srcName 原来的名字
	 * @param destName 更新的名字
	 * @throws IOException 
	 */
	@RequestMapping(value = "reName", method = POST, produces = "text/plain; charset=utf-8")
	@ResponseBody
	public void reName(String srcName, String destName) throws IOException {
		String srcap = upDownloader.getAbsolutePath(getRelativePath(srcName));
		String destap = upDownloader.getAbsolutePath(getRelativePath(destName));
		File src = new File(srcap);
		File dest = new File(destap);
		if (src.exists()) {
			synchronized (fileMutex) {
				src.renameTo(dest);
				fileSearch.deleteIndex(new File(srcap));
				fileSearch.updateIndex(new File(destap));
			}
		}
	}
	
	/**
	 * 删除目录或文件
	 * @param filename
	 * @throws IOException 
	 */
	@RequestMapping(value = "delete", method = POST, produces = "text/plain; charset=utf-8")
	@ResponseBody
	public void delete(String filename) throws IOException {
		String absolutePath = upDownloader.getAbsolutePath(getRelativePath(filename));
		synchronized (fileMutex) {
			UpDownloader.deleteDir(absolutePath);
			fileSearch.deleteIndex(new File(absolutePath));
		}
	}
	
	/**
	 * 前端用到FormData对象提交multipart formdata数据，所以需要对中文编码
	 * @param path
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "resource", method = POST, produces = "text/plain; charset=utf-8")
	@ResponseBody
	public String uploadFile(@RequestPart("path") String path, @RequestPart("file") Part file) throws IOException {
		String dir = URLDecoder.decode(path, "UTF-8");
		String fullname, filename = file.getSubmittedFileName();
		if (dir.endsWith(File.separator)) {
			fullname = dir + filename;
		} else {
			fullname = dir + File.separator + filename;
		}
		fullname = getRelativePath(fullname);
		String absolutePath = upDownloader.upload(fullname, file);
		fileSearch.addIndex(new File(absolutePath));
		return filename + ": 上传成功!";
	}
	
	/**
	 * 获取系统支持的字符集
	 * @return
	 */
	@RequestMapping(value = "availableCharsets", method = GET)
	@ResponseBody
	public Set<String> availableCharsets() {
		return textUtil.availableCharsets();
	}
	
	/**
	 * 获取指定路径的文本内容
	 * @param path
	 * @param charset
	 * @return
	 */
	@RequestMapping(value = "loadText", method = POST, produces = "text/plain; charset=utf-8")
	@ResponseBody
	public String loadText(@RequestParam(value = "path", required = true) String path
			, @RequestParam(value = "charset", required = false, defaultValue = "UTF-8") String charset) {
		String absolutePath = upDownloader.getAbsolutePath(getRelativePath(path));
		return textUtil.getText(new File(absolutePath), charset);
	}
	
	@RequestMapping(value = "writeText", method = POST, produces = "text/plain; charset=utf-8")
	@ResponseBody
	public void writeText(@RequestBody Form form) throws IOException {
		synchronized (textUpdateMutex) {
			String absolutePath = upDownloader.getAbsolutePath(getRelativePath(form.getPath()));
			File f = new File(absolutePath);
			textUtil.writeText(f, form.getTextContext(), form.getCharset());
			fileSearch.updateIndex(new File(absolutePath));
		}
	}
	
	/**
	 * 前端传来的路径是CMS_DIR/aaa/bbb
	 * 将该路径转为相对于cmsRoot的路径：aaa/bbb
	 * 这样才能便于UpDownloader使用
	 * 
	 * @param path
	 * @return
	 */
	private String getRelativePath(String path) {
		Matcher m = cmsRoot_pattern.matcher(path);
		if (!m.find()) {
			throw new BadRequestException("传入的路径不是：" + CMS_DIR + "路径");
		}
		String relativePath = m.replaceFirst("");;
		return UpDownloader.getSystemPath(relativePath);
	}
	/*
	public static void main(String[] args) {
		String dir = "cms_dir/new node1";
		Pattern startAtCmsRoot = Pattern.compile("(^" + PATTERN_SEPARATOR + "cms_dir" + PATTERN_SEPARATOR + "?)|(^cms_dir" + PATTERN_SEPARATOR + "?)");
		Matcher m = startAtCmsRoot.matcher(dir);
		System.out.println(m.find());
		System.out.println(m.replaceFirst(""));
		
	}*/
	
	@SuppressWarnings("unused")
	private static class Form implements Serializable {
		private static final long serialVersionUID = 968705461440871636L;
		String path;
		String textContext;
		String charset;
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public String getTextContext() {
			return textContext;
		}
		public void setTextContext(String textContext) {
			this.textContext = textContext;
		}
		public String getCharset() {
			return charset;
		}
		public void setCharset(String charset) {
			this.charset = charset;
		}
		@Override
		public String toString() {
			return "Form [path=" + path + ", textContext=" + textContext + ", charset=" + charset + "]";
		}
	}
	
}
