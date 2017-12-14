package com.github.emailtohl.integration.core.user.entities;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.core.user.UserType;

/**
 * 用户实体经常被外部引用，但由于信息量较大，加载性能低，本实体存储必要信息，并应用用户实体
 * 为了被外部关联查询到用户实体本身，UserRef本身也得是实体，且必须与User的继承配置一致
 * 
 * @author HeLei
 */
@org.hibernate.envers.Audited
@Entity
@Table(name = "users_ref")
@Access(AccessType.PROPERTY)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class UserRef implements Serializable {
	private static final long serialVersionUID = -1980610697330696766L;
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
	
	public UserRef() {}
	
	public UserRef(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.nickname = user.getNickname();
		this.email = user.getEmail();
		if (user.getImage() != null) {
			this.icon = user.getImage().getPath();
		}
		this.cellPhone = user.getCellPhone();
	}

	@Id
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Transient
	public UserType getUserType() {
		if (this instanceof EmployeeRef) {
			return UserType.Employee;
		}
		if (this instanceof CustomerRef) {
			return UserType.Customer;
		}
		if (this instanceof UserRef) {
			return UserType.User;
		}
		return null;
	}
	public void setUserType(UserType userType) {}

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
	
	@Column(unique = true, /*nullable = false, */updatable = true)
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	@org.hibernate.envers.NotAudited
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	@Column(name = "cell_phone", /*nullable = false, */unique = true, updatable = true)
	public String getCellPhone() {
		return cellPhone;
	}
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		UserRef other = (UserRef) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		return true;
	}
	
}
