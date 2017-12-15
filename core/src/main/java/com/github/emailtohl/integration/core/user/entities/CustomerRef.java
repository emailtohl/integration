package com.github.emailtohl.integration.core.user.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * 用户实体经常被外部引用，但由于信息量较大，加载性能低，本实体存储必要信息，并应用用户实体
 * @author HeLei
 */
@org.hibernate.search.annotations.Indexed
@org.hibernate.envers.Audited
@Entity
@Table(name = "customer_ref")
public class CustomerRef extends UserRef {
	private static final long serialVersionUID = 6385160931228910996L;
	protected Customer customer;

	public CustomerRef() {
		super();
	}
	
	public CustomerRef(Customer customer) {
		super(customer);
		this.customer = customer;
	}
	
	@org.hibernate.envers.NotAudited
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
