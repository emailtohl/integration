package com.github.emailtohl.integration.core.user.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.jpa.entity.EnumBridgeCust;
import com.github.emailtohl.integration.core.file.Image;

/**
 * 客户，如顾客、商家、匿名访问者等等
 * 他们注册时需要手机号唯一识别
 * @author HeLei
 */
@org.hibernate.search.annotations.Indexed
@org.hibernate.envers.Audited
@Entity
@Table(name = "customer")
public class Customer extends User {
	private static final long serialVersionUID = -1136305533524407299L;
	/**
	 * 内置账号匿名
	 */
	public static final String ANONYMOUS_NAME = "anonymous";
	/**
	 * 内置账号匿名邮箱
	 */
	public static final String ANONYMOUS_EMAIL = "anonymous@localhost";
	/**
	 * 客户的等级
	 * @author HeLei
	 */
	public enum Level {
		ORDINARY, VIP
	}
	
	/**
	 * 顾客等级
	 */
	private Level level;
	/**
	 * 地址
	 */
	private Address address;
	/**
	 * 身份证号码
	 */
	@Pattern(regexp = ConstantPattern.IDENTIFICATION)
	private String identification;
	/**
	 * 积分
	 */
	private Integer points;
	/**
	 * 拥有的卡
	 */
	private Set<Card> cards = new HashSet<Card>();
	/**
	 * 顾客分类
	 */
	private Classify classify;
	/**
	 * 自身引用
	 */
	private CustomerRef customerRef;
	
	/**
	 * 客户可能用多种方式登录，如邮箱、手机号等等……
	 * 为了便于查询，创建客户与多个标识信息的关联
	 * 当客户添加email或cellPhone时，自动为其添加进usernames
	 */
	private Set<String> usernames = new HashSet<String>();
	
	@org.hibernate.search.annotations.Field(bridge = @org.hibernate.search.annotations.FieldBridge(impl = EnumBridgeCust.class))
	@Enumerated(EnumType.STRING)
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
	
	@org.hibernate.search.annotations.IndexedEmbedded(depth = 1)
	@Embedded
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	
	@org.hibernate.search.annotations.Field
	@Column(unique = true)
	public String getIdentification() {
		return identification;
	}
	public void setIdentification(String identification) {
		this.identification = identification;
	}
	
	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	@ElementCollection
	@JoinTable(name = "customer_card")
	public Set<Card> getCards() {
		return cards;
	}
	public void setCards(Set<Card> cards) {
		this.cards = cards;
	}

	@org.hibernate.search.annotations.Field(bridge = @org.hibernate.search.annotations.FieldBridge(impl = EnumBridgeCust.class))
	public Classify getClassify() {
		return classify;
	}
	public void setClassify(Classify classify) {
		this.classify = classify;
	}
	
	@org.hibernate.envers.NotAudited
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "customer")
	public CustomerRef getCustomerRef() {
		return customerRef;
	}
	public void setCustomerRef(CustomerRef customerRef) {
		this.customerRef = customerRef;
	}
	
	@org.hibernate.envers.NotAudited
	@ElementCollection
	@CollectionTable(name = "customer_username", joinColumns = @JoinColumn(name = "customer_id"))
	@Column(name = "unique_name", nullable = false, unique = true)
	public Set<String> getUsernames() {
		return usernames;
	}
	public void setUsernames(Set<String> usernames) {
		this.usernames = usernames;
	}
	
	@Override
	public void setId(Long id) {
		super.id = id;
		if (customerRef != null) {
			customerRef.id = id;
		}
	}
	@Override
	public void setName(String name) {
		super.name = name;
		if (customerRef != null) {
			customerRef.name = name;
		}
	}
	@Override
	public void setNickname(String nickname) {
		super.nickname = nickname;
		if (customerRef != null) {
			customerRef.nickname = nickname;
		}
	}
	@Override
	public void setEmail(String email) {
		super.email = email;
		if (customerRef != null) {
			customerRef.email = email;
		}
		refreshUsernames();
	}
	@Override
	public void setImage(Image image) {
		super.image = image;
		if (customerRef != null && image != null) {
			customerRef.iconSrc = image.getSrc();
		}
	}
	@Override
	public void setCellPhone(String cellPhone) {
		super.cellPhone = cellPhone;
		if (customerRef != null) {
			customerRef.cellPhone = cellPhone;
		}
		refreshUsernames();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cellPhone == null) ? 0 : cellPhone.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Customer))
			return false;
		Customer other = (Customer) obj;
		if (cellPhone == null) {
			if (other.getCellPhone() != null)
				return false;
		} else if (!cellPhone.equals(other.getCellPhone()))
			return false;
		if (email == null) {
			if (other.getEmail() != null)
				return false;
		} else if (!email.equals(other.getEmail()))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Customer [level=" + level + ", address=" + address + ", identification=" + identification + ", points="
				+ points + ", cards=" + cards + ", classify=" + classify + ", name=" + name + ", email=" + email
				+ ", cellPhone=" + cellPhone + ", enabled=" + enabled + ", id=" + id + "]";
	}
	
	private void refreshUsernames() {
		this.usernames.clear();
		if (super.email != null && !super.email.isEmpty()) {
			this.usernames.add(super.getEmail());
		}
		if (super.cellPhone != null && !super.cellPhone.isEmpty()) {
			this.usernames.add(super.getCellPhone());
		}
	}
}

