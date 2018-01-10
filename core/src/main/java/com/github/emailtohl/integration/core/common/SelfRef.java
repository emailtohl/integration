package com.github.emailtohl.integration.core.common;

/**
 * 自引用接口，有的实体对象有自引用，从而形成树形的数据结构，由于较为特殊，故抽象出来
 * 
 * @author HeLei
 */
public interface SelfRef {
	
	/**
	 * 属性访问器
	 * @return
	 */
	SelfRef getParent();
	
	/**
	 * 判断本节点是否另外一个节点的祖先
	 * @param other
	 * @return
	 */
	default boolean isAncestorOf(SelfRef other) {
		boolean b = false;
		SelfRef parent = other.getParent();
		while (parent != null) {
			if (this.equals(parent)) {
				b = true;
				break;
			}
			parent = parent.getParent();
		}
		return b;
	}
}
