package com.github.emailtohl.integration.nuser.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;

/**
 * 公司实体
 * 
 * @author HeLei
 * @date 2017.02.04
 */
@Entity
@Table(name = "company")
public class Company extends BaseEntity {
	private static final long serialVersionUID = 2560110793039918070L;
	private String name;
	private String description;
	private transient Set<Department> departments;
	private Company parent;

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

	@OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(Set<Department> departments) {
		this.departments = departments;
	}

	@ManyToOne
	@JoinColumn(name = "parent_id")
	public Company getParent() {
		return parent;
	}

	public void setParent(Company parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "Company [name=" + name + ", description=" + description + "]";
	}

}