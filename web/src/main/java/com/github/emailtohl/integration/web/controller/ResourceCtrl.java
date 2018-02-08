package com.github.emailtohl.integration.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.tree.ZtreeNode;
import com.github.emailtohl.integration.core.file.FileService;

/**
 * 资源管理控制器
 * @author HeLei
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ResourceCtrl {
	private static final Logger logger = LogManager.getLogger();
	private static final String CUSTOMER_SPACE_NAME = "customerSpace";
	@Inject
	FileService fileService;
	@Inject
	@Named("resources")
	File resources;
	/**
	 * 用户空间
	 */
	File customerSpace;
	
	@PostConstruct
	public void init() throws IOException {
		customerSpace = new File(resources, CUSTOMER_SPACE_NAME);
		if (!customerSpace.exists()) {
			customerSpace.mkdir();
		}
	}
	
	/**
	 * 测试该文件或目录是否存在
	 * @param pathname 目录+文件名
	 * @return
	 */
	public String exist(String pathname) {
		boolean b = fileService.exist(getFilePath(pathname));
		return String.format("{\"exist\":%b}", b);
	}
	
	/**
	 * 获取资源管理的根目录的数据结构
	 * @return
	 */
/*	@RequestMapping(value = "root", method = RequestMethod.GET)
	public List<ZtreeNode> getNodes() {
		fileService
	}*/
	
	/**
	 * 将基于resources的File转成相对于resources的路径
	 * @param f
	 * @return
	 */
/*	public String getPath(File f) {
		
	}*/
	
	/**
	 * 创建文件夹
	 * @param dirname
	 * @return
	 */
/*	public ExecResult createDir(String dirname) {
		
	}*/
	
	/**
	 * 更改文件或文件夹名
	 * @param srcName 目录+文件名
	 * @param destName 目录+文件名
	 * @return
	 */
/*	public ExecResult reName(String srcName, String destName) {
		
	}*/
	
	/**
	 * 删除文件或文件夹
	 * @param pathname 目录+文件名
	 * @return
	 */
/*	public ExecResult delete(String pathname) {
		
	}*/
	
	/**
	 * 前端的路径是“/”，后端若是Windows系统，则在文件系统中是“\”，需要进行统一处理
	 * @param pathname
	 * @return
	 */
	public static String getFilePath(String pathname) {
		String path = String.join(File.separator, pathname.split(ConstantPattern.SEPARATOR));
		if (path.startsWith(File.separator)) {
			path = CUSTOMER_SPACE_NAME + path;
		} else {
			path = CUSTOMER_SPACE_NAME + File.separator + path;
		}
		return path;
	}
	
}
