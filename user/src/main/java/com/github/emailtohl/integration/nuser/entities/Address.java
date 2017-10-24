package com.github.emailtohl.integration.nuser.entities;

import javax.persistence.Embeddable;

/**
 * 地址嵌入类
 * @author HeLei
 * @date 2017.10.17
 */
@Embeddable
public class Address {
	private String city;
	private String zipcode;
	private String street;
	
	public Address() {
	}
	public Address(String city, String zipcode, String street) {
		super();
		this.city = city;
		this.zipcode = zipcode;
		this.street = street;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	
}
