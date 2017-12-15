package com.github.emailtohl.integration.core.user.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Min;

import org.hibernate.search.bridge.builtin.IntegerBridge;

/**
 * 用户实体经常被外部引用，但由于信息量较大，加载性能低，本实体存储必要信息，并应用用户实体
 * @author HeLei
 */
@org.hibernate.search.annotations.Indexed
@org.hibernate.envers.Audited
@Entity
@Table(name = "employee_ref")
public class EmployeeRef extends UserRef {
	private static final long serialVersionUID = 757273856079174616L;
	protected Integer empNum;
	protected Employee employee;
	
	public EmployeeRef() {
		super();
	}
	
	public EmployeeRef(Employee employee) {
		super(employee);
		empNum = employee.getEmpNum();
		this.employee = employee;
	}

	@org.hibernate.search.annotations.Field(bridge = @org.hibernate.search.annotations.FieldBridge(impl = IntegerBridge.class))
	@org.hibernate.envers.NotAudited
	@Column(name = "emp_num", unique = true, updatable = false)
	@Min(value = Employee.NO_BOT)
	public Integer getEmpNum() {
		return empNum;
	}
	public void setEmpNum(Integer empNum) {
		this.empNum = empNum;
	}

	@org.hibernate.envers.NotAudited
	// 若要代理延迟加载，仅在Hibernate清楚存在链接才合理，若属性是可为空，则必须去数据库查询，既然需访问数据库，查询不然早加载
	// 要使用代理实现延迟加载，optional应为false，这与JPA规范一致
	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	public Employee getEmployee() {
		return employee;
	}
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	@Override
	public String toString() {
		return "EmployeeRef [id=" + id + ", empNum=" + empNum + ", name=" + name + ", nickname=" + nickname + ", email="
				+ email + ", icon=" + icon + ", cellPhone=" + cellPhone + "]";
	}

}
