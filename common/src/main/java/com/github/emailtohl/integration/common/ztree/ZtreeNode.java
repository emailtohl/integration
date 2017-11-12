package com.github.emailtohl.integration.common.ztree;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * 前端zTree的数据模型
 * 
 * @author HeLei
 */
public class ZtreeNode<T> implements Serializable, Comparable<ZtreeNode<T>> {
	private static final long serialVersionUID = -1014084140766054734L;
	protected transient static volatile long serial = 0;
	/** id 自动生成 */
	protected final long id;
	
	/** pid 父节点，根节点为0 */
	protected long pid = 0;
	
	/** 节点名 */
	protected String name;
	
	/** 绑定的属性，可在ztree的setting.data.key中定义： {attribute:{}}*/
	protected T attribute;
	
	/** 记录 treeNode 节点是否为父节点 */
	protected boolean isParent = true;
	
	/** 判断 treeNode 节点是否被隐藏 */
	protected boolean isHidden = false;
	
	/** 记录 treeNode 节点的 展开 / 折叠 状态 */
	protected boolean open = false;
	
	/** 节点的 checkBox / radio 的 勾选状态 [setting.check.enable = true & treeNode.nocheck = false 时有效] */
	protected boolean checked = false;
	
	/** 设置节点的 checkbox / radio 是否禁用 [setting.check.enable = true 时有效] */
	protected boolean chkDisabled = false;
	
	/** 设置节点是否隐藏 checkbox / radio [setting.check.enable = true 时有效] */
	protected boolean nocheck = false;
	
	/** 节点自定义图标的 URL 路径 */
	protected String icon;
	
	/** 父节点自定义折叠时图标的 URL 路径 */
	protected String iconClose;
	
	/** 父节点自定义展开时图标的 URL 路径 */
	protected String iconOpen;
	
	/** 节点自定义图标的 className */
	protected String iconSkin;
	
	/** 设置点击节点后在何处打开 url。[treeNode.url 存在时有效] 例如：{ "id":1, "name":"test1", "url":"http://myTest.com", "target":"_blank"} */
	protected String target;
	
	/** 节点链接的目标 URL */
	protected String url;
	
	/** 节点的子节点数据集合， 在前端，如果是文件而非目录，该字段应该为null，所以此处不初始化 */
	protected Set<ZtreeNode<T>> children;
	
	/** 自定义属性，用于标识查询结果 */
	protected boolean selected;
	
	public ZtreeNode() {
		synchronized(ZtreeNode.class) {
			if (serial == Long.MAX_VALUE) {
				serial = 0;
			}
			serial++;
			id = serial;
		}
	}

	public static long getSerial() {
		return serial;
	}

	public static void setSerial(long serial) {
		ZtreeNode.serial = serial;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public T getAttribute() {
		return attribute;
	}

	public void setAttribute(T attribute) {
		this.attribute = attribute;
	}

	public boolean isParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isChkDisabled() {
		return chkDisabled;
	}

	public void setChkDisabled(boolean chkDisabled) {
		this.chkDisabled = chkDisabled;
	}

	public boolean isNocheck() {
		return nocheck;
	}

	public void setNocheck(boolean nocheck) {
		this.nocheck = nocheck;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIconClose() {
		return iconClose;
	}

	public void setIconClose(String iconClose) {
		this.iconClose = iconClose;
	}

	public String getIconOpen() {
		return iconOpen;
	}

	public void setIconOpen(String iconOpen) {
		this.iconOpen = iconOpen;
	}

	public String getIconSkin() {
		return iconSkin;
	}

	public void setIconSkin(String iconSkin) {
		this.iconSkin = iconSkin;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Set<ZtreeNode<T>> getChildren() {
		return children;
	}

	public void setChildren(Set<ZtreeNode<T>> children) {
		this.children = children;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public long getId() {
		return id;
	}

	/**
	 * 根据queue的顺序逐级打开ZtreeNode的节点
	 * 
	 * @param nodes
	 * @param openPath 沿着打开的路径
	 */
	public void setOpen(Set<? extends ZtreeNode<T>> nodes, List<String> openPath) {
		class Func {
			LinkedList<String> queue;

			Func(List<String> queue) {
				this.queue = new LinkedList<String>(queue);
			}

			void setOpen(Set<? extends ZtreeNode<T>> nodes) {
				String name = queue.poll();
				for (ZtreeNode<T> node : nodes) {
					if (node.isParent) {
						if (name != null && name.equals(node.name)) {
							node.open = true;
							// 根据定义node.isParent == true，那么node.children !=
							// null，不过保险起见还是做判断
							if (node.children != null) {
								setOpen(node.children);
							}
						}
					} else {
						if (name != null && name.equals(node.name)) {
							node.selected = true;
						}
					}
				}
			}
		}
		new Func(openPath).setOpen(nodes);
	}
	
	@Override
	public int compareTo(ZtreeNode<T> o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		ZtreeNode<T> other = (ZtreeNode<T>) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "ZtreeNode [name=" + name + ", children=" + children + "]";
	}

	/**
	 * 根据Node接口返回一个ztree
	 * @param nodes
	 * @return
	 */
	public static Set<ZtreeNode<Node>> getZtreeNodes(Collection<Node> nodes) {
		Map<String, ZtreeNode<Node>> map = new HashMap<String, ZtreeNode<Node>>();
		for (Node n : nodes) {
			ZtreeNode<Node> z = new ZtreeNode<Node>();
			z.name = n.getName();
			z.attribute = n;
			z.isParent = false;
			map.put(n.getKey(), z);
		}
		for (ZtreeNode<Node> z : map.values()) {
			Node parent = z.getAttribute().getParent();
			if (parent != null) {
				ZtreeNode<Node> parentZn = map.get(parent.getKey());
				if (parentZn != null) {
					parentZn.isParent = true;
					if (parentZn.children == null) {
						parentZn.children = new TreeSet<ZtreeNode<Node>>();
					}
					parentZn.children.add(z);
				}
			}
		}
		for (Iterator<Entry<String, ZtreeNode<Node>>> i = map.entrySet().iterator(); i.hasNext();) {
			Entry<String, ZtreeNode<Node>> e = i.next();
			if (!e.getValue().isParent) {
				i.remove();
			}
		}
		return new TreeSet<ZtreeNode<Node>>(map.values());
	}
}
