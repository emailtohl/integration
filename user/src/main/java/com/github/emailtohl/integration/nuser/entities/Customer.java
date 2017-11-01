package com.github.emailtohl.integration.nuser.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.github.emailtohl.integration.common.Constant;

/**
 * 系统外部使用人员，如顾客、商家、匿名访问者等等
 * 他们注册时需要手机号唯一识别
 * @author HeLei
 */
@org.hibernate.search.annotations.Indexed
@org.hibernate.envers.Audited
@Entity
@Table(name = "customer")
public class Customer extends User {
	private static final long serialVersionUID = -1136305533524407299L;
	
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
	@Pattern(regexp = Constant.PATTERN_IDENTIFICATION)
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
	 * 注册的顾客，一般是以电话号码识别，所以号码不能为空，且号码具有唯一性
	 * 若顾客更换手机，号码可以改变
	 * 手机，重要识别标志
	 */
	@org.hibernate.search.annotations.Field
	@NotNull
	public String getCellPhone() {
		return super.cellPhone;
	}
	
	public void setCellPhone(String cellPhone) {
		super.cellPhone = cellPhone;
	}
	
	@org.hibernate.search.annotations.Field
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
	@JoinTable(name = "t_customer_card")
	public Set<Card> getCards() {
		return cards;
	}
	public void setCards(Set<Card> cards) {
		this.cards = cards;
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
		if (getClass() != obj.getClass())
			return false;
		Customer other = (Customer) obj;
		if (cellPhone == null) {
			if (other.cellPhone != null)
				return false;
		} else if (!cellPhone.equals(other.cellPhone))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		return true;
	}
	
}

