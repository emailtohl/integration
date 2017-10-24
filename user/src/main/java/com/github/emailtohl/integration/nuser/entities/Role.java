package com.github.emailtohl.integration.nuser.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
/**
 * 角色实体类
 * @author HeLei
 * @date 2017.02.04
 */
@org.hibernate.envers.Audited
@Entity
@Table(name = "role")
public class Role extends BaseEntity {
	private static final long serialVersionUID = 5715974372158270885L;
	
	public Role() {
		super();
	}
	
	public Role(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	@NotNull
	private String name;
	private String description;
	private transient Set<User> users = new HashSet<User>();
	private Set<Authority> authorities = new HashSet<Authority>();
    /**
     * 若角色已被用户使用，所以就不能删除，该标记用于不能删除的角色
     */
	private Boolean canBeDeleted;
	
	@Column(nullable = false, unique = true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@org.hibernate.envers.NotAudited
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
	@JoinTable(name = "t_role_authority"
	, joinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id") }
	, inverseJoinColumns = { @JoinColumn(name = "authority_id", referencedColumnName = "id") })
	public Set<Authority> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(Set<Authority> authorities) {
		this.authorities = authorities;
	}

	@Transient
	public Boolean getCanBeDeleted() {
		return canBeDeleted;
	}

	public void setCanBeDeleted(Boolean canBeDeleted) {
		this.canBeDeleted = canBeDeleted;
	}

	@Override
	public String toString() {
		return "Role [name=" + name + ", authorities=" + authorities + "]";
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
	
}
