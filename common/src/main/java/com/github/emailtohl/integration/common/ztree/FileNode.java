package com.github.emailtohl.integration.common.ztree;

import java.io.File;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.github.emailtohl.integration.common.ConstantPattern;

/**
 * 前端zTree的数据模型
 * 
 * @author HeLei
 */
public class FileNode extends ZtreeNode<File> {
	private static final long serialVersionUID = -1932148922352477076L;
	
	private FileNode() {}
	
	/**
	 * 根据文件目录创建一个ZtreeNode实例
	 * @param absolutePath 文件目录
	 * @return ZtreeNode实例
	 */
	public static FileNode newInstance(String absolutePath) {
		return newInstance(new File(absolutePath));
	}
	
	/**
	 * 根据文件目录创建一个ZtreeNode实例
	 * @param f 文件目录
	 * @return ZtreeNode实例
	 */
	public static FileNode newInstance(File f) {
		return newInstance(f, 0);
	}
	
	private static FileNode newInstance(File f, long pid) {
		FileNode n = new FileNode();
		n.name = f.getName();
		n.pid = pid;
		n.attribute = f;
		if (f.isDirectory()) {
			Set<ZtreeNode<File>> children = new TreeSet<ZtreeNode<File>>();
			for (File sf : f.listFiles()) {
				children.add(newInstance(sf, n.id));
			}
			n.children = children;
			n.isParent = true;
		} else {
			n.isParent = false;
		}
		return n;
	}

	/** 记录 treeNode 节点是否为父节点，依赖于children属性 */
	@Override
	public boolean isParent() {
		return children != null;
	}

	/**
	 * 根据路径匹配，打开对应的目录
	 * @param path
	 */
	public void setOpen(String path) {
		LinkedList<String> queue = new LinkedList<String>();
		for (String name : path.split(ConstantPattern.SEPARATOR)) {
			queue.add(name);
		}
		Set<FileNode> nodes = new TreeSet<FileNode>();
		nodes.add(this);
		setOpen(nodes, queue);
	}

	@Override
	public String toString() {
		return "FileNode [name=" + name + ", open=" + open + ", children=" + children + ", selected=" + selected + "]";
	}
	
}
