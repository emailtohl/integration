package com.github.emailtohl.integration.core.user.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;

/**
 * 公司实体
 * 
 * @author HeLei
 */
@Entity
@Table(name = "company")
public class Company extends BaseEntity {
	private static final long serialVersionUID = 2560110793039918070L;
	private String name;
	private String description;
	private String responsiblePerson;
	private transient Set<Department> departments = new HashSet<Department>();
	private Company parent;

	public Company() {}
	
	public Company(String name, String description, Company parent) {
		this.name = name;
		this.description = description;
		this.parent = parent;
	}

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
	
	public String getResponsiblePerson() {
		return responsiblePerson;
	}
	public void setResponsiblePerson(String responsiblePerson) {
		this.responsiblePerson = responsiblePerson;
	}

	@OneToMany(mappedBy = "company", orphanRemoval = true)
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
		return "Company [name=" + name + ", description=" + description + ", responsiblePerson=" + responsiblePerson
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Company other = (Company) obj;
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;
		return true;
	}

}
