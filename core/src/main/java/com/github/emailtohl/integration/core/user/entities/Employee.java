package com.github.emailtohl.integration.core.user.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;

import org.hibernate.search.bridge.builtin.DoubleBridge;
import org.hibernate.search.bridge.builtin.IntegerBridge;

import com.github.emailtohl.integration.core.file.Image;

/**
 * 系统平台账号，他们有工号唯一识别
 * @author HeLei
 */
@org.hibernate.search.annotations.Indexed
@org.hibernate.envers.Audited
@Entity
@Table(name = "employee")
public class Employee extends User {
	private static final long serialVersionUID = 3500096827826710751L;
	/**
	 * 最低序号
	 */
	public static final int NO1 = 1000;
	/**
	 * 内置账号
	 */
	public static final int NO_BOT = NO1 - 1;
	/**
	 * 内置账号管理员名字
	 */
	public static final String ADMIN_NAME = "admin";
	/**
	 * 内置账号管理员邮箱
	 */
	public static final String ADMIN_EMAIL = "admin@localhost";
	/**
	 * 内置账号机器人名字
	 */
	public static final String BOT_NAME = "bot";
	/**
	 * 内置账号机器人邮箱
	 */
	public static final String BOT_EMAIL = "bot@localhost";
	/**
	 * 4-7位数字默认为工号
	 */
	public static final String PATTERN_EMP_NUM = "(^\\d\\d{2,5}\\d$)|(" + NO_BOT + ")";
	/**
	 * 工号
	 */
	private Integer empNum;
	/**
	 * 职位
	 */
	private String post;
	/**
	 * 薪水
	 */
	private Double salary;
	/**
	 * 部门
	 */
	private Department department;
	/**
	 * 自身引用
	 */
	private EmployeeRef employeeRef;
	
	@org.hibernate.search.annotations.Field(bridge = @org.hibernate.search.annotations.FieldBridge(impl = IntegerBridge.class))
//	@org.hibernate.search.annotations.NumericField
	@org.hibernate.envers.NotAudited
	@Column(name = "emp_num", unique = true/*, nullable = false*/, updatable = false)
	@Min(value = NO_BOT)
	public Integer getEmpNum() {
		return empNum;
	}
	public void setEmpNum(Integer empNum) {
		this.empNum = empNum;
		if (employeeRef != null) {
			employeeRef.empNum = empNum;
		}
	}
	
	@org.hibernate.search.annotations.Field
	public String getPost() {
		return post;
	}
	public void setPost(String post) {
		this.post = post;
	}
	
	@org.hibernate.search.annotations.Field(bridge = @org.hibernate.search.annotations.FieldBridge(impl = DoubleBridge.class))
//	@org.hibernate.search.annotations.NumericField
	public Double getSalary() {
		return salary;
	}
	public void setSalary(Double salary) {
		this.salary = salary;
	}
	
	@org.hibernate.search.annotations.IndexedEmbedded(depth = 1)
	@ManyToOne
	@JoinColumn(name = "department_id")
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}
	
	@org.hibernate.envers.NotAudited
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "employee")
	public EmployeeRef getEmployeeRef() {
		return employeeRef;
	}
	public void setEmployeeRef(EmployeeRef employeeRef) {
		this.employeeRef = employeeRef;
	}
	
	@Override
	public void setId(Long id) {
		super.id = id;
		if (employeeRef != null) {
			employeeRef.id = id;
		}
	}
	@Override
	public void setName(String name) {
		super.name = name;
		if (employeeRef != null) {
			employeeRef.name = name;
		}
	}
	@Override
	public void setNickname(String nickname) {
		super.nickname = nickname;
		if (employeeRef != null) {
			employeeRef.nickname = nickname;
		}
	}
	@Override
	public void setEmail(String email) {
		super.email = email;
		if (employeeRef != null) {
			employeeRef.email = email;
		}
	}
	@Override
	public void setImage(Image image) {
		super.image = image;
		if (employeeRef != null && image != null) {
			employeeRef.iconSrc = image.getSrc();
		}
	}
	@Override
	public void setCellPhone(String cellPhone) {
		super.cellPhone = cellPhone;
		if (employeeRef != null) {
			employeeRef.name = name;
		}
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
		if (!(obj instanceof Employee))
			return false;
		Employee other = (Employee) obj;
		if (empNum == null) {
			if (other.getEmpNum() != null)
				return false;
		} else if (!empNum.equals(other.getEmpNum()))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Employee [empNum=" + empNum + ", post=" + post + ", salary=" + salary + ", department=" + department
				+ ", name=" + name + ", email=" + email + ", cellPhone=" + cellPhone + ", enabled=" + enabled + ", id="
				+ id + "]";
	}

}
