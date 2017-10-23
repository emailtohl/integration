package com.github.emailtohl.integration.common.ztree;

/**
 * 节点需要实现的接口
 * 
 * @author HeLei
 * @date 2017.02.04
 */
public interface Node {
	/**
	 * 用于节点显示
	 * @return
	 */
	String getName();
	/**
	 * 唯一识别该节点的
	 * @return
	 */
	String getKey();
	/**
	 * 获取父级节点
	 * @return
	 */
	Node getParent();
}
