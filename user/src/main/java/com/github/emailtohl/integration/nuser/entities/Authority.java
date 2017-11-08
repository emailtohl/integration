package com.github.emailtohl.integration.nuser.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
/**
 * 角色关联的授权
 * @author HeLei
 */
@org.hibernate.envers.Audited
@org.hibernate.annotations.BatchSize(size = 10)// 因n+1查询问题，盲猜优化，一次性加载size个代理
@Entity
@Table(name = "authority")
public class Authority extends BaseEntity {
	private static final long serialVersionUID = 2353467451352218773L;
	
	/**
	 * 管理角色的权限
	 */
	public static final String ROLE = "role";
	
	/**
	 * 查询所有用户的权限
	 */
	public static final String QUERY_ALL_USER = "query_all_user";
	
	/**
	 * 管理平台账号的权限
	 */
	public static final String EMPLOYEE = "employee";
	
	/**
	 * 为平台账号授予角色的权限
	 */
	public static final String EMPLOYEE_ROLE = "employee_role";
	
	/**
	 * 启用平台账号的权限
	 */
	public static final String EMPLOYEE_ENABLED = "employee_enabled";
	
	/**
	 * 为平台账号重置密码的权限
	 */
	public static final String EMPLOYEE_RESET_PASSWORD = "employee_reset_password";
	
	/**
	 * 删除平台账号的权限
	 */
	public static final String EMPLOYEE_DELETE = "employee_delete";
	
	/**
	 * 管理客户的权限
	 */
	public static final String CUSTOMER = "customer";
	
	/**
	 * 为客户授予角色的权限
	 */
	public static final String CUSTOMER_ROLE = "customer_role";
	
	/**
	 * 为客户提升等级的权限
	 */
	public static final String CUSTOMER_LEVEL = "customer_level";
	
	/**
	 * 启用客户账号的权限
	 */
	public static final String CUSTOMER_ENABLED = "customer_enabled";
	
	/**
	 * 为客户重置密码的权限
	 */
	public static final String CUSTOMER_RESET_PASSWORD = "customer_reset_password";
	
	/**
	 * 删除客户的权限
	 */
	public static final String CUSTOMER_DELETE = "customer_delete";
	
	/**
	 * 流程权限
	 */
	public static final String FLOW = "flow";
	
	/**
	 * 处理申请单状态的权限
	 */
	public static final String APPLICATION_FORM_TRANSIT = "application_form_transit";
	
	/**
	 * 删除申请单
	 */
	public static final String APPLICATION_FORM_DELETE = "application_form_delete";
	
	/**
	 * 查询申请单的处理历史
	 */
	public static final String APPLICATION_FORM_READ_HISTORY = "application_form_read_history";
	
	/**
	 * 删除论坛帖子
	 */
	public static final String FORUM_DELETE = "forum_delete";
	
	/**
	 * 审计修改用户信息
	 */
	public static final String AUDIT_USER = "audit_user";
	
	/**
	 * 审计修改角色信息
	 */
	public static final String AUDIT_ROLE = "audit_role";
	
	/**
	 * 资源管理，文件上传，目录创建、改名以及删除
	 */
	public static final String RESOURCE_MANAGER = "resource_manager";
	
	/**
	 * 内容管理权限
	 */
	public static final String CONTENT_MANAGER = "content_manager";
	
	public Authority() {
		super();
	}
	public Authority(String name, String description, Authority parent) {
		super();
		this.name = name;
		this.description = description;
		this.parent = parent;
	}
	
	@NotNull
	private String name;
	private Authority parent;
	private String description;
	private transient Set<Role> roles = new HashSet<Role>();
	
	@Column(nullable = false, unique = true, updatable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@ManyToOne
	@JoinColumn(name = "parent_id")
	public Authority getParent() {
		return parent;
	}
	public void setParent(Authority parent) {
		this.parent = parent;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@org.hibernate.envers.NotAudited
	@org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
	@ManyToMany(mappedBy = "authorities")
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	/**
	 * 返回本权限以及本权限父权限的名字
	 * @return
	 */
	public Set<String> authorityNames() {
		Set<String> names = new HashSet<String>();
		names.add(name);
		Authority p = getParent();
		while (p != null) {
			names.add(p.getName());
			p = p.getParent();
		}
		return names;
	}
	
	@Override
	public String toString() {
		return "Authority [name=" + name + "]";
	}
	
	/**
	 * 基于唯一标识name的equals和hashCode方法
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof Authority))
			return false;
		final Authority that = (Authority) other;
		if (this.name == null || that.getName() == null)
			return false;
		else
			return this.name.equals(that.getName());
	}
}
