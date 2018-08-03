package com.github.emailtohl.integration.core.common;

/**
 * 持有Parent的节点
 * @author HeLei
 */
public interface Node {
	/**
	 * 唯一识别该节点的
	 * @return
	 */
	String getKey();
	/**
	 * 用于节点显示
	 * @return
	 */
	String getName();
	/**
	 * 获取父级节点
	 * @return
	 */
	Node getParent();
}
