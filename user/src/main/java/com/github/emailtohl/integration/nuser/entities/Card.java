package com.github.emailtohl.integration.nuser.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * 支付卡实体类
 * @author HeLei
 * @date 2017.10.17
 */
@Embeddable
public class Card {
	public enum Type {
		BankAccount, CreditCard
	}
	
	private Type type;
	
	private String number;
	
	private Date expDate;

	public Card() {
	}

	public Card(Type type, String number) {
		this.type = type;
		this.number = number;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	@Column(nullable = false, unique = true)
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Date getExpDate() {
		return expDate;
	}

	public void setExpDate(Date expDate) {
		this.expDate = expDate;
	}
	
}
