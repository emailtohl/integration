package com.github.emailtohl.integration.common.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 处理tree结构的工具
 * 
 * @author HeLei
 */
public final class TreeFunc {
	/**
	 * 从持有Parent的node集合中生成Ztree的数据结构
	 * @param nodes
	 * @return
	 */
	public static List<ZtreeNode> getZtreeNode(Collection<Node> nodes) {
		List<ZtreeNode> result = new ArrayList<ZtreeNode>();
		Map<String, Node> nmap = new HashMap<String, Node>();
		Map<String, ZtreeNode> zmap = new HashMap<String, ZtreeNode>();
		for (Node n : nodes) {
			nmap.put(n.getKey(), n);
			ZtreeNode z = new ZtreeNode(n.getKey(), n.getName());
			zmap.put(n.getKey(), z);
			if (n.getParent() == null) {
				result.add(z);
			}
		}
		// 建立children关系
		for (Entry<String, ZtreeNode> e : zmap.entrySet()) {
			String key = e.getKey();
			Node node = nmap.get(key);
			if (node == null) {
				continue;
			}
			Node parent = node.getParent();
			if (parent == null) {
				continue;
			}
			ZtreeNode parentZn = zmap.get(parent.getKey());
			if (parentZn == null) {
				continue;
			}
			parentZn.isParent = true;
			parentZn.children.add(e.getValue());
		}
		return result;
	}
	
	/**
	 * 从文件系统中获取与之对应的Ztree的数据结构
	 * @param files
	 * @return
	 */
	public static List<ZtreeNode> getZtreeNodeByFilesystem(Collection<File> files) {
		List<ZtreeNode> result = new ArrayList<ZtreeNode>();
		Map<String, ZtreeNode> zmap = new HashMap<String, ZtreeNode>();
		List<File> _files = new ArrayList<File>();
		for (File f : files) {
			ZtreeNode z = new ZtreeNode(f.getPath(), f.getName());
			if (f.isDirectory()) {
				z.isParent = true;
				_files.addAll(Arrays.asList(f.listFiles()));
			}
			zmap.put(z.getKey(), z);
			result.add(z);
		}
		// 广度优先遍历目录树
		while (!_files.isEmpty()) {
			List<File> items = _files;
			_files = new ArrayList<File>();
			for (File f : items) {
				// 创建子节点
				ZtreeNode z = new ZtreeNode(f.getPath(), f.getName());
				// 把新建的节点添加到映射中
				zmap.put(z.getKey(), z);
				// 若子节点仍是目录，则将其下的所有文件添加进队列
				if (f.isDirectory()) {
					z.isParent = true;
					_files.addAll(Arrays.asList(f.listFiles()));
				}
				// 建立Children关系
				String parentPath = f.getParent();
				ZtreeNode parent = zmap.get(parentPath);
				if (parent == null) {
					continue;
				}
				parent.getChildren().add(z);
			}
		}
		return result;
	}
	
	/**
	 * 对其排序
	 * @param nodes
	 * @param comp
	 */
	public static void sort(List<ZtreeNode> nodes, Comparator<ZtreeNode> comp) {
		nodes.sort(comp);
		List<ZtreeNode> _nodes = new ArrayList<ZtreeNode>(nodes);
		while (!_nodes.isEmpty()) {
			List<ZtreeNode> items = _nodes;
			_nodes = new ArrayList<ZtreeNode>();
			for (ZtreeNode node : items) {
				node.children.sort(comp);
				_nodes.addAll(node.children);
			}
		}
	}
}
