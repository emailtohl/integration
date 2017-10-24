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
/**
 * 系统外部使用人员，如顾客、商家、匿名访问者等等
 * 他们注册时需要手机号唯一识别
 * @author HeLei
 * @date 2017.02.04
 */
@org.hibernate.envers.Audited
@Entity
@Table(name = "customer")
public class Customer extends User {
	private static final long serialVersionUID = -1136305533524407299L;
	
	public enum Level {
		ORDINARY, VIP
	}
	// 顾客等级
	private Level level;
	// 地址
	private Address address;
	// 身份证号码
	private String identification;
	// 拥有的卡
	private Set<Card> cards = new HashSet<Card>();
	
	/**
	 * 注册的顾客，一般是以电话号码识别，所以号码不能为空，且号码具有唯一性
	 * 若顾客更换手机，号码可以改变
	 */
	@Column(nullable = false, unique = true, updatable = true)
	@Override
	public String getTelephone() {
		return telephone;
	}
	@Override
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
	@Enumerated(EnumType.STRING)
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
	
	@Embedded
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	
	@Column(unique = true)
	public String getIdentification() {
		return identification;
	}
	public void setIdentification(String identification) {
		this.identification = identification;
	}
	
	@ElementCollection
	@JoinTable(name = "t_customer_card")
	public Set<Card> getCards() {
		return cards;
	}
	public void setCards(Set<Card> cards) {
		this.cards = cards;
	}
	
}
