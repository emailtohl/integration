package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.utils.ZtreeNode;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.file.FileService;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 资源管理控制器
 * @author HeLei
 */
@RestController
@RequestMapping(value = "resource", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ResourceCtrl {
	private static final Logger logger = LogManager.getLogger();
	private static final String USER_SPACE_NAME = "userSpace";
	private Gson gson = new GsonBuilder().setPrettyPrinting().addSerializationExclusionStrategy(new ExclusionStrategy() {
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			if (f.getName().equals("key")) {
				return true;
			}
			return false;
		}
		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	}).create();
	@Inject
	private FileService fileService;
	@Inject
	@Named("resources")
	private File resources;
	/**
	 * 用户空间
	 */
	private File userSpace;
	
	@PostConstruct
	public void init() throws IOException {
		userSpace = new File(resources, USER_SPACE_NAME);
		if (!userSpace.exists()) {
			userSpace.mkdir();
		}
	}
	
	/**
	 * 测试该文件或目录是否存在
	 * @param pathname 目录+文件名
	 * @return
	 */
	@RequestMapping(value = "exist", method = GET)
	public String exist(@RequestParam(name = "pathname") String pathname) {
		boolean b = fileService.exist(getFilePath(pathname));
		return String.format("{\"exist\":%b}", b);
	}
	
	/**
	 * 查询文本内容
	 * @param param 内容的字符串
	 * @return 路径集合
	 */
	@RequestMapping(value = "query", method = GET)
	public String queryResources(@RequestParam(name = "param", required = false, defaultValue = "") String param) {
		List<ZtreeNode> nodes = fileService.findFile(param);
		ZtreeNode userSpaceRoot = null;
		for (ZtreeNode n : nodes) {
			if (n.getName().equals(userSpace.getName())) {
				userSpaceRoot = n;
				break;
			}
		}
		if (userSpaceRoot != null) {
			return gson.toJson(userSpaceRoot.getChildren());
		} else {
			return "[]";
		}
	}
	
	/**
	 * 创建一个目录
	 * @param dirName 目录相对路径
	 */
	@RequestMapping(value = "createDir", method = POST)
	public ExecResult createDir(String dirName) {
		return fileService.createDir(getFilePath(dirName));
	}
	
	/**
	 * 为目录或文件改名
	 * @param srcName 原来的名字
	 * @param destName 更新的名字
	 * @throws IOException 
	 */
	@RequestMapping(value = "reName", method = POST)
	public ExecResult reName(String srcName, String destName) throws IOException {
		return fileService.reName(getFilePath(srcName), getFilePath(destName));
	}
	
	/**
	 * 删除目录或文件
	 * @param filename
	 * @throws IOException 
	 */
	@RequestMapping(value = "delete", method = POST)
	public ExecResult delete(String filename) throws IOException {
		return fileService.delete(getFilePath(filename));
	}
	
	/**
	 * 前端用到FormData对象提交multipart formdata数据，所以需要对中文编码
	 * @param path
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "file", method = POST)
	public ExecResult uploadFile(@RequestPart("path") String path, @RequestPart("file") Part file) throws IOException {
		String filename = URLDecoder.decode(path, "UTF-8");
		filename = getFilePath(filename) + File.separator + file.getSubmittedFileName();
		try (InputStream in = file.getInputStream()) {
			return fileService.save(filename, in);
		}
	}
	
	/**
	 * 批量上传文件，若包含请求参数：uploadPath，则保存在指定目录中，否则保存在自动保存目录中
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "files", method = POST)
	public ExecResult multipartOnload(HttpServletRequest request) {
		StringBuilder msg = new StringBuilder();
		Collection<Part> fileParts = null;
		Map<String, String[]> map = request.getParameterMap();
		try {
			fileParts = request.getParts();
		} catch (IOException | ServletException e) {
			logger.catching(e);
			return new ExecResult(false, e.getMessage(), "");
		}
		for (Iterator<Part> iterable = fileParts.iterator(); iterable.hasNext();) {
			Part filePart = iterable.next();// 每个filePart表示一个文件，前端可能同时上传多个文件
			try {
				String submittedFileName = filePart.getSubmittedFileName();// 获取提交文件原始的名字
				if (submittedFileName != null && !map.containsKey(submittedFileName)) {
					try (InputStream in = filePart.getInputStream()) {
						String uploadPath = request.getParameter("uploadPath");
						ExecResult execResult;
						if (uploadPath != null) { // 若有uploadPath参数（不管是否空字符串）则存储在用户空间中
							execResult = fileService.save(getFilePath(uploadPath) + File.separator + submittedFileName, in);
						} else { // 否则存储在自动空间中
							execResult = fileService.autoSaveFile(in, null);
						}
						if (!execResult.ok) {
							msg.append(',').append(submittedFileName).append(": fail");
						} else {
							msg.append(',').append(submittedFileName).append(": success");
						}
					}
				}
			} catch (IOException e) {
				logger.catching(e);
				if (filePart != null) {
					try {
						filePart.delete();
					} catch (IOException e1) {
						logger.catching(e1);
					}
				}
				return new ExecResult(false, e.getMessage(), "");
			}
		}
		return new ExecResult(true, "", msg.toString());
	}
	
	/**
	 * 获取系统支持的字符集
	 * @return
	 */
	@RequestMapping(value = "availableCharsets", method = GET)
	public Set<String> availableCharsets() {
		return Charset.availableCharsets().keySet();
	}
	
	/**
	 * 获取指定路径的文本内容
	 * @param path
	 * @param charset
	 * @return
	 */
	@RequestMapping(value = "loadText", method = POST, produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
	public String loadText(@RequestParam(value = "path", required = true) String path
			, @RequestParam(value = "charset", required = false, defaultValue = "UTF-8") String charset) {
		File f = fileService.getFile(getFilePath(path));
		String result = "";
		if (f != null && f.exists()) {
			try {
				result = FileUtils.readFileToString(f, charset);
			} catch (IOException e) {
				logger.catching(e);
			}
		}
		return result;
	}
	
	@RequestMapping(value = "writeText", method = POST)
	public ExecResult writeText(@RequestBody Form form) throws IOException {
		Charset cset;
		try {
			cset = Charset.forName(form.charset);
		} catch (IllegalArgumentException e) {
			cset = Charset.defaultCharset();
			logger.info("使用默认编码：" + cset.displayName() + "读取文件");
		}
		ByteBuffer buffer = cset.encode(form.textContext);
		byte[] bytes = buffer.array();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		return fileService.save(getFilePath(form.getPath()), in);
	}
	
	/**
	 * 统一将“\”，“/”两种路径表达方式替换为本地操作系统识别的路径符
	 * 字符串结尾没有文件分隔符
	 * @param pathname
	 * @return
	 */
	public String getFilePath(String pathname) {
		String path = String.join(File.separator, pathname.split(ConstantPattern.SEPARATOR));
		if (path.startsWith(File.separator)) {
			path = USER_SPACE_NAME + path;
		} else {
			path = USER_SPACE_NAME + File.separator + path;
		}
		return path;
	}
	
	/**
	 * 表单结构
	 * @author HeLei
	 */
	@SuppressWarnings("unused")
	private static class Form implements Serializable {
		private static final long serialVersionUID = 6035589078250860658L;
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
