package com.github.emailtohl.integration.core.file;

import static com.github.emailtohl.integration.common.ConstantPattern.LEGAL_FILENAME;
import static com.github.emailtohl.integration.core.role.Authority.CONTENT;
import static com.github.emailtohl.integration.core.role.Authority.RESOURCE;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.Pattern;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.utils.ZtreeNode;
import com.github.emailtohl.integration.core.ExecResult;

/**
 * 资源文件管理服务接口
 * 本服务已经统一定向到系统中资源文件的存储目录下，所有输入的路径名（文件名）均是相对于该根目录的相对路径
 * @author HeLei
 */
@Validated
public interface FileService {
	/**
	 * 测试该文件或目录是否存在
	 * @param pathname 目录+文件名
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	boolean exist(String pathname);
	
	/**
	 * 根据filename相对路径获取到系统中的定位的File
	 * @param pathname 目录+文件名
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	File getFile(String pathname);
	
	/**
	 * 将基于resources的File，转成相对于resources的路径
	 * @param f
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	String getPath(File f);
	
	/**
	 * 创建文件夹
	 * @param dirname
	 * @return 仅仅是创建成功或失败的标记
	 */
	@PreAuthorize("hasAuthority('" + RESOURCE + "')")
	ExecResult createDir(@Pattern(regexp = LEGAL_FILENAME) String dirname);
	
	/**
	 * 更改文件或文件夹名
	 * @param srcName 目录+文件名
	 * @param destName 目录+文件名
	 * @return 仅仅是修改成功或失败的标记
	 */
	@PreAuthorize("hasAuthority('" + RESOURCE + "')")
	ExecResult reName(String srcName, @Pattern(regexp = LEGAL_FILENAME) String destName);
	
	/**
	 * 删除文件或文件夹
	 * @param pathname 目录+文件名
	 * @return 仅仅是删除成功或失败的标记
	 */
	@PreAuthorize("hasAuthority('" + RESOURCE + "')")
	ExecResult delete(String pathname);
	
	/**
	 * 保存文件到，原文件可以不存在
	 * @param pathname 目录+文件名
	 * @param in 文件输入流
	 * @return 仅仅是保存成功或失败的标记
	 */
	ExecResult save(@Pattern(regexp = LEGAL_FILENAME) String pathname, InputStream in);
	
	/**
	 * 根据内部存储情况自动存储文件，适用于图片等资料
	 * @param in 文件输入流
	 * @param suffix 文件后缀，若为null，则不保存文件后缀
	 * @return ExecResult.attribute中存储返回存储后的文件的路径+文件名，文件名由自动计算
	 */
	ExecResult autoSaveFile(InputStream in, String suffix);
	
	/**
	 * 根据文件名或文本内容查询目录树
	 * @param query
	 * @return
	 */
	List<ZtreeNode> findFile(String query);
	
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
	 * 读取文本文件
	 * @param pathname 目录+文件名
	 * @param charset the charset to use, null means platform default
	 * @return 若加载失败则返回的ok是false；成功则将内容存入attribute中
	 */
	ExecResult loadText(String pathname, String charset);
	
	/**
	 * 写入文本文件
	 * @param pathname 目录+文件名
	 * @param textContext
	 * @param charset the charset to use, null means platform default
	 * @return 仅仅是写入成功或失败的标记
	 */
	@PreAuthorize("hasAuthority('" + CONTENT + "')")
	ExecResult writeText(@Pattern(regexp = LEGAL_FILENAME) String pathname, String textContext, String charset);

}
