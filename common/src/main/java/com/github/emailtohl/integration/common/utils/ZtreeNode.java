package com.github.emailtohl.integration.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 符合Ztree的数据结构
 * 
 * @author HeLei
 */
public class ZtreeNode implements Serializable {
	private static final long serialVersionUID = -7530502845259404928L;
	
	/**
	 * 自定义属性，用于唯一识别本类的实例
	 */
	protected final String key;
	
	/**
	 * 自定义属性，用于标识查询结果
	 */
	protected boolean selected;
	
	/**
	 * 节点名称。如果不使用 name 属性保存节点名称，请修改 setting.data.key.name
	 * 默认值：无
	 */
	protected String name;
	
	/**
	 * 记录 treeNode 节点是否为父节点。
	 * 1、初始化节点数据时，根据 treeNode.children 属性判断，有子节点则设置为 true，否则为 false
	 * 2、初始化节点数据时，如果设定 treeNode.isParent = true，即使无子节点数据，也会设置为父节点
	 */
	protected boolean isParent = false;
	
	/**
	 * 记录 treeNode 节点的 展开 / 折叠 状态。
	 * 默认值：false
	 */
	protected boolean open = false;
	
	/**
	 * 节点的子节点数据集合。如果不使用 children 属性保存子节点数据，请修改 setting.data.key.children
	 * 默认值：无
	 */
	protected List<ZtreeNode> children = new ArrayList<ZtreeNode>();
	
	/**
	 * 节点的 checkBox / radio 的 勾选状态。[setting.check.enable = true & treeNode.nocheck =
	 * false 时有效]
	 * 1、如果不使用 checked 属性设置勾选状态，请修改 setting.data.key.checked
	 * 2、建立 treeNode 数据时设置 treeNode.checked = true 可以让节点的输入框默认为勾选状态
	 * 3、修改节点勾选状态，可以使用 treeObj.checkNode / checkAllNodes / updateNode
	 * 方法，具体使用哪种请根据自己的需求而定
	 * 默认值：false
	 */
	protected boolean checked = false;
	
	/**
	 * 1、设置节点的 checkbox / radio 是否禁用 [setting.check.enable = true 时有效]
	 * 2、为了解决部分朋友生成 json 数据出现的兼容问题, 支持 "false","true" 字符串格式的数据
	 * 3、请勿对已加载的节点修改此属性，禁止 或 取消禁止 请使用 setChkDisabled() 方法
	 * 4、初始化时，如果需要子孙节点继承父节点的 chkDisabled 属性，请设置 setting.check.chkDisabledInherit 属性
	 * 默认值：false
	 */
	protected boolean chkDisabled = false;
	
	/**
	 * 最简单的 click 事件操作。相当于 onclick="..." 的内容。 如果操作较复杂，请使用 onClick 事件回调函数。
	 * 默认值：无
	 */
	protected String click;
	
	/**
	 * 强制节点的 checkBox / radio 的 半勾选状态。[setting.check.enable = true &
	 * treeNode.nocheck = false 时有效]
	 * 1、强制为半勾选状态后，不再进行自动计算半勾选状态
	 * 2、设置 treeNode.halfCheck = false 或 null 才能恢复自动计算半勾选状态
	 */
	protected boolean halfCheck = false;
	
	/**
	 * 节点自定义图标的 URL 路径。
	 * 1、父节点如果只设置 icon ，会导致展开、折叠时都使用同一个图标
	 * 2、父节点展开、折叠使用不同的个性化图标需要同时设置 treeNode.iconOpen / treeNode.iconClose 两个属性
	 * 3、如果想利用 className 设置个性化图标，需要设置 treeNode.iconSkin 属性
	 * 默认值：无
	 */
	protected String icon;
	
	/**
	 * 父节点自定义折叠时图标的 URL 路径。
	 * 1、此属性只针对父节点有效
	 * 2、此属性必须与 iconOpen 同时使用
	 * 3、如果想利用 className 设置个性化图标，需要设置 treeNode.iconSkin 属性
	 * 默认值：无
	 */
	protected String iconClose;
	
	/**
	 * 父节点自定义展开时图标的 URL 路径。
	 * 1、此属性只针对父节点有效
	 * 2、此属性必须与 iconClose 同时使用
	 * 3、如果想利用 className 设置个性化图标，需要设置 treeNode.iconSkin 属性
	 * 默认值：无
	 */
	protected String iconOpen;
	
	/**
	 * 节点自定义图标的 className
	 * 默认值：无
	 */
	protected String iconSkin;
	
	/**
	 * 判断 treeNode 节点是否被隐藏。
	 * 1、初始化 zTree 时，如果节点设置 isHidden = true，会被自动隐藏
	 * 2、请勿对已加载的节点修改此属性，隐藏 / 显示 请使用 hideNode() / hideNodes() / showNode() /
	 * showNodes() 方法
	 */
	protected Boolean isHidden = null;
	
	/**
	 * 设置节点是否隐藏 checkbox / radio [setting.check.enable = true 时有效]
	 * 默认值：false
	 */
	protected boolean nocheck = false;
	
	/**
	 * 设置点击节点后在何处打开 url。[treeNode.url 存在时有效]
	 * 默认值：无
	 */
	protected String target;
	
	/**
	 * 节点链接的目标 URL
	 * 1、编辑模式 (setting.edit.enable = true) 下此属性功能失效，如果必须使用类似功能，请利用 onClick
	 * 事件回调函数自行控制。
	 * 2、如果需要在 onClick 事件回调函数中进行跳转控制，那么请将 URL 地址保存在其他自定义的属性内，请勿使用 url
	 * 默认值：无
	 */
	protected String url;
	
	/**
	 * 自定义挂载的数据信息
	 */
	protected Object attribute;

	public String getKey() {
		return key;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public List<ZtreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<ZtreeNode> children) {
		this.children = children;
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

	public String getClick() {
		return click;
	}

	public void setClick(String click) {
		this.click = click;
	}

	public boolean isHalfCheck() {
		return halfCheck;
	}

	public void setHalfCheck(boolean halfCheck) {
		this.halfCheck = halfCheck;
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

	public Boolean getIsHidden() {
		return isHidden;
	}

	public void setIsHidden(Boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isNocheck() {
		return nocheck;
	}

	public void setNocheck(boolean nocheck) {
		this.nocheck = nocheck;
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

	public Object getAttribute() {
		return attribute;
	}

	public void setAttribute(Object attribute) {
		this.attribute = attribute;
	}

	/**
	 * 构造
	 * @param key
	 */
	public ZtreeNode(String key) {
		this.key = key;
	}
	/**
	 * 构造
	 * @param key
	 */
	public ZtreeNode(String key, String name) {
		this(key);
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		ZtreeNode other = (ZtreeNode) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ZtreeNode [key=" + key + ", name=" + name + ", isParent=" + isParent + ", children=" + children
				+ ", open=" + open + "]";
	}

}
