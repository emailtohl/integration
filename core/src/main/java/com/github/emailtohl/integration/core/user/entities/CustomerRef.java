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
@Table(name = "customer_ref")
public class CustomerRef implements Serializable {
	private static final long serialVersionUID = 6385160931228910996L;
	protected Long id;
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
	protected Customer customer;

	public CustomerRef() {}
	
	public CustomerRef(Customer customer) {
		id = customer.getId();
		name = customer.getName();
		nickname = customer.getNickname();
		email = customer.getEmail();
		cellPhone = customer.getCellPhone();
		icon = customer.getImage() == null ? customer.getImage().getPath() : null;
		this.customer = customer;
	}
	
	@Id
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@Override
	public String toString() {
		return "CustomerRef [id=" + id + ", name=" + name + ", nickname=" + nickname + ", email=" + email + ", icon="
				+ icon + ", cellPhone=" + cellPhone + "]";
	}
	
}
