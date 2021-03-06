package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDate;
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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.common.ZtreeNode;
import com.github.emailtohl.integration.core.file.FileService;
import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.lib.ConstantPattern;
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
	public static final String ICON_SPACE = "iconSpace";
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
	@Inject
	private UserService userService;
	@Inject
	private EmployeeService employeeService;
	@Inject
	private CustomerService customerService;
	
	/**
	 * 用户空间
	 */
	private File userSpace;
	/**
	 * 头像空间
	 */
	private File iconSpace;
	
	@PostConstruct
	public void init() throws IOException {
		userSpace = new File(resources, USER_SPACE_NAME);
		if (!userSpace.exists()) {
			userSpace.mkdir();
		}
		iconSpace = new File(resources, ICON_SPACE);
		if (!iconSpace.exists()) {
			iconSpace.mkdir();
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
		if (msg.length() > 0 && msg.charAt(0) == ',') {
			msg.deleteCharAt(0);
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
		ExecResult execResult = fileService.loadText(getFilePath(path), charset);
		if (execResult.ok) {
			return (String) execResult.attribute;
		} else {
			return execResult.cause;
		}
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
	 * 上传图片,针对前端CKEditor接口编写的控制器方法
	 * @param image
	 * @return 返回一个CKEditor识别的回调函数
	 * @throws IOException
	 */
	@RequestMapping(value = "image", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void uploadImage(@RequestParam("CKEditorFuncNum") String CKEditorFuncNum/* 回调显示图片的位置 */, 
			@RequestPart("upload") Part image
			, HttpServletResponse response) throws IOException {
		ExecResult execResult = null;
		try (InputStream in = image.getInputStream()) {
			String submittedFileName = image.getSubmittedFileName();
			String suffix = FilenameUtils.getExtension(submittedFileName);
			execResult = fileService.autoSaveFile(in, suffix);
		}
		String html;
		if (execResult.ok) {
			String url = (String) execResult.attribute;
			if (StringUtils.hasText(url)) {
				url = String.join("/", url.split(ConstantPattern.SEPARATOR));
			}
			url = resources.getName() + "/" + url;
			html = "<script type=\"text/javascript\">window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",'" + url + "','');</script>";
		} else {
			// 第三个参数为空表示没有错误，不为空则会弹出一个对话框显示　error　message　的内容
			html = "<script type=\"text/javascript\">window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",'','上传失败');</script>";
		}
		response.addHeader("X-Frame-OPTIONS", "SAMEORIGIN");
		response.setContentType("text/html; charset=utf-8");  
        try (PrintWriter out = response.getWriter()) {
        	out.println(html);
        }
	}
	
	/**
	 * 用户上传头像
	 * @param icon
	 * @throws IOException 
	 */
	@RequestMapping(value = "icon", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void uploadIcon(@RequestParam("id") long id, @RequestPart("icon") Part icon) throws IOException {
		User u = userService.get(id);
		if (u == null) {
			return;
		}
		// 删除原有的图片，且同步数据库中的信息
		String iconSrc = u.getImage() == null ? null : u.getImage().getSrc();
		if (StringUtils.hasText(iconSrc)) {
			fileService.delete(iconSrc);
		}
		
		LocalDate date = LocalDate.now();
		String name = icon.getSubmittedFileName();
		String path = ICON_SPACE + File.separator + date.getYear() + File.separator + date.getDayOfYear();
		path = path + File.separator + id + '_' + name;
		try (InputStream in = icon.getInputStream()) {
			ExecResult execResult = fileService.save(path, in);
			if (execResult.ok) {
				String src = resources.getName() + "/" + String.join("/", path.split(ConstantPattern.SEPARATOR));
				Image img = new Image(name, src);
				u.setImage(img);
				if (u instanceof Employee) {
					employeeService.update(id, (Employee) u);
				} else if (u instanceof Customer) {
					customerService.update(id, (Customer) u);
				}
			}
		}
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
