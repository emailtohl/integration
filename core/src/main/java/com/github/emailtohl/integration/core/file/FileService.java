package com.github.emailtohl.integration.core.file;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.ztree.FileNode;
import com.github.emailtohl.integration.core.ExecResult;

/**
 * 文件管理服务接口
 * 本服务已经统一定向到系统中文件存储的根目录下，所有输入的路径名（文件名）均是相对于该根目录的相对路径
 * @author HeLei
 */
@PreAuthorize("isAuthenticated()")
public interface FileService {
	/**
	 * 测试该文件或目录是否存在
	 * @param filename
	 * @return
	 */
	boolean exist(String filename);
	
	/**
	 * 根据filename相对路径获取到系统中的定位的File
	 * @param filename
	 * @return
	 */
	File getFile(String filename);
	
	/**
	 * 创建文件夹
	 * @param dirname
	 * @return
	 */
	ExecResult createDir(String dirname);
	
	/**
	 * 更改文件或文件夹名
	 * @param srcName
	 * @param destName
	 * @return
	 */
	ExecResult reName(String srcName, String destName);
	
	/**
	 * 删除文件或文件夹
	 * @param filename
	 * @return
	 */
	ExecResult delete(String filename);
	
	/**
	 * 保存文件到
	 * @param filename
	 * @param in
	 * @return
	 */
	ExecResult save(String filename, InputStream in);
	
	/**
	 * 根据文件名或文本内容查询目录树
	 * @param query
	 * @return
	 */
	Set<FileNode> findFile(String query);
	
	/**
	 * 重新索引
	 */
	void reIndex();
	
	/**
	 * 获取系统支持的字符集
	 * @return
	 */
	Set<String> availableCharsets();
	
	/**
	 * 默认以UTF-8加载文本文件
	 * @param filename
	 * @param charset the charset to use, null means platform default
	 * @return 若加载失败则返回的ok是false；成功则将内容存入attribute中
	 */
	ExecResult loadText(String filename, String charset);
	
	/**
	 * 写入文本文件
	 * @param filename
	 * @param textContext
	 * @param charset the charset to use, null means platform default
	 * @return
	 */
	ExecResult writeText(String filename, String textContext, String charset);
}
