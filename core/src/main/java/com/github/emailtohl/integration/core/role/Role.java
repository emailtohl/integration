package com.github.emailtohl.integration.core.role;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.lib.jpa.BaseEntity;
import com.github.emailtohl.lib.jpa.EnumBridgeCust;
/**
 * 角色实体类
 * @author HeLei
 */
@org.hibernate.envers.Audited
@Entity
@Table(name = "role")
public class Role extends BaseEntity {
	private static final long serialVersionUID = 5715974372158270885L;
	/**
	 * 系统内置超级管理员
	 */
	public static final String ADMIN = "admin";
	
	public Role() {
		super();
	}
	
	public Role(String name, RoleType roleType, String description) {
		super();
		this.name = name;
		this.roleType = roleType;
		this.description = description;
	}

	@NotNull
	private String name;
	private String description;
	private RoleType roleType;
	private transient Set<User> users = new HashSet<User>();
	private Set<Authority> authorities = new HashSet<Authority>();
	
	@org.hibernate.search.annotations.Field
	@Column(nullable = false, unique = true, updatable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@org.hibernate.search.annotations.Field
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@org.hibernate.search.annotations.Field(bridge = @org.hibernate.search.annotations.FieldBridge(impl = EnumBridgeCust.class))
	// 枚举存入数据库默认为序号，这里指明将枚举名以字符串存入数据库
	@Enumerated(EnumType.STRING)
	@Column(name = "role_type")
	public RoleType getRoleType() {
		return roleType;
	}
	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}

	@JsonBackReference
	@org.hibernate.envers.NotAudited
	// 配合@IndexedEmbedded使用，保证Lucene document的联动更新当前类被用JPA方式标注为@Embeddable时不需要使用@ContainedIn
	@org.hibernate.search.annotations.ContainedIn
	// Hibernate的@Fetch(FetchMode.SUBSELECT)注解只能用于懒加载的集合，它将n+1查询转成两次查询，一次查询Role自身，拿到Role的id后第二次嵌套查询User：
	// SELECT * FROM t_user u WHERE u.id IN (SELECT ur.user_id FROM t_user_role ur WHERE ur.role_id = ?)
	@org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
	@ManyToMany(mappedBy = "roles")
	public Set<User> getUsers() {
		return users;
	}
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	
	@ManyToMany
	@JoinTable(name = "role_authority"
	, joinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id") }
	, inverseJoinColumns = { @JoinColumn(name = "authority_id", referencedColumnName = "id") })
	public Set<Authority> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(Set<Authority> authorities) {
		this.authorities = authorities;
	}

	public Set<String> authorityNames() {
		Set<String> names = new HashSet<String>();
		getAuthorities().forEach(a -> names.addAll(a.authorityNames()));
		return names;
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
		if (!(other instanceof Role))
			return false;
		final Role that = (Role) other;
		if (this.name == null || that.getName() == null)
			return false;
		else
			return this.name.equals(that.getName());
	}

	@Override
	public String toString() {
		return "Role [name=" + name + ", description=" + description + ", roleType=" + roleType + ", id=" + id + "]";
	}
	
}
