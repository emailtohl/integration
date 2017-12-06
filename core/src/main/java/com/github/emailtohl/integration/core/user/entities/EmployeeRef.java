package com.github.emailtohl.integration.core.user.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;

import com.github.emailtohl.integration.common.ConstantPattern;

/**
 * 用户实体经常被外部引用，但由于信息量较大，加载性能低，本实体存储必要信息，并应用用户实体
 * @author HeLei
 */
@Entity
@Table(name = "employee_ref")
public class EmployeeRef implements Serializable {
	private static final long serialVersionUID = 757273856079174616L;
	protected Long id;
	protected Integer empNum;
	protected String name;
	protected String nickname;// 可存储第三方昵称
	@Pattern(// 校验
		regexp = ConstantPattern.EMAIL,
		flags = {Pattern.Flag.CASE_INSENSITIVE}
	)
	protected String email;
	protected String icon;
	@Pattern(regexp = ConstantPattern.CELL_PHONE)
	protected String cellPhone;
	protected Employee employee;
	
	public EmployeeRef() {}
	
	public EmployeeRef(Employee employee) {
		id = employee.getId();
		empNum = employee.getEmpNum();
		name = employee.getName();
		nickname = employee.getNickname();
		email = employee.getEmail();
		cellPhone = employee.getCellPhone();
		icon = employee.getImage() != null ? employee.getImage().getPath() : null;
		this.employee = employee;
	}

	@Id
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Integer getEmpNum() {
		return empNum;
	}
	public void setEmpNum(Integer empNum) {
		this.empNum = empNum;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getCellPhone() {
		return cellPhone;
	}
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

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
