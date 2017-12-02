package com.github.emailtohl.integration.core.user.entities;

import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;

import com.github.emailtohl.integration.common.ConstantPattern;

/**
 * 地址嵌入类
 * @author HeLei
 */
@Embeddable
public class Address {
	private String city;
	
	@Pattern(regexp = ConstantPattern.ZIPCODE)
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
	
	@org.hibernate.search.annotations.Field
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	@org.hibernate.search.annotations.Field
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	
	@org.hibernate.search.annotations.Field
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((zipcode == null) ? 0 : zipcode.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (zipcode == null) {
			if (other.zipcode != null)
				return false;
		} else if (!zipcode.equals(other.zipcode))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Address [city=" + city + ", zipcode=" + zipcode + ", street=" + street + "]";
	}
	
}
