package com.github.emailtohl.integration.core.file;

import java.io.InputStream;
import java.util.Set;

import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.common.ztree.FileNode;

/**
 * 文件管理服务接口
 * @author HeLei
 */
public interface FileService {
	/**
	 * 根据文件名或文本内容查询目录树
	 * @param query
	 * @return
	 */
	Set<FileNode> findFile(String query);
	
	/**
	 * 创建文件夹
	 * @param path
	 * @return
	 */
	ExecResult createDir(String path);
	
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
	 * @param path
	 * @param in
	 * @return
	 */
	ExecResult save(String path, InputStream in);
}
