package com.github.emailtohl.integration.nuser.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;

/**
 * 系统内部使用人员，他们有工号唯一识别
 * @author HeLei
 * @date 2017.02.04
 */
@org.hibernate.envers.Audited
@Entity
@Table(name = "employee")
public class Employee extends User {
	private static final long serialVersionUID = 3500096827826710751L;
	private Integer empNum;
	private String post;
	private Double salary;
	private Department department;
	
	@Column(name = "emp_num", unique = true/*, nullable = false*/, updatable = false)
	@Min(value = 1)
	public Integer getEmpNum() {
		return empNum;
	}
	public void setEmpNum(Integer empNum) {
		this.empNum = empNum;
	}
	public String getPost() {
		return post;
	}
	public void setPost(String post) {
		this.post = post;
	}
	public Double getSalary() {
		return salary;
	}
	public void setSalary(Double salary) {
		this.salary = salary;
	}
	
	@org.hibernate.envers.NotAudited
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "department_id")
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((empNum == null) ? 0 : empNum.hashCode());
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
		Employee other = (Employee) obj;
		if (empNum == null) {
			if (other.empNum != null)
				return false;
		} else if (!empNum.equals(other.empNum))
			return false;
		return true;
	}
	
}
