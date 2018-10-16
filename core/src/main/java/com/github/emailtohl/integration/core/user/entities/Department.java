package com.github.emailtohl.integration.core.user.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.emailtohl.integration.core.common.SelfRef;
import com.github.emailtohl.lib.jpa.EntityBase;

/**
 * 部门实体
 * 
 * @author HeLei
 */
@org.hibernate.envers.Audited
@Entity
@Table(name = "department")
public class Department extends EntityBase implements SelfRef {
	private static final long serialVersionUID = -4263959308837757530L;
	private String name;
	private String description;
	private String responsiblePerson;
	private transient Set<Employee> employees = new HashSet<Employee>();
	private Department parent;
	private Company company;

	public Department() {}
	
	public Department(String name, String description, Department parent) {
		super();
		this.name = name;
		this.description = description;
		this.parent = parent;
	}

	@org.hibernate.search.annotations.Field
	@Column(nullable = false, unique = true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@org.hibernate.search.annotations.Field
	@org.hibernate.envers.NotAudited
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "responsible_person")
	public String getResponsiblePerson() {
		return responsiblePerson;
	}
	public void setResponsiblePerson(String responsiblePerson) {
		this.responsiblePerson = responsiblePerson;
	}

	@JsonBackReference
	@org.hibernate.envers.NotAudited
	@org.hibernate.search.annotations.ContainedIn
	@OneToMany(mappedBy = "department")
	public Set<Employee> getEmployees() {
		return employees;
	}
	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

	@org.hibernate.envers.NotAudited
	@ManyToOne
	@JoinColumn(name = "parent_id")
	public Department getParent() {
		return parent;
	}
	public void setParent(Department parent) {
		this.parent = parent;
	}

	@org.hibernate.envers.NotAudited
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "company_id")
	public Company getCompany() {
		return company;
	}
	public void setCompany(Company company) {
		this.company = company;
	}

	@Override
	public String toString() {
		return "Department [name=" + name + ", description=" + description + ", responsiblePerson=" + responsiblePerson
				+ "]";
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
		if (!(other instanceof Department))
			return false;
		final Department that = (Department) other;
		if (this.name == null || that.getName() == null)
			return false;
		else
			return this.name.equals(that.getName());
	}
}
