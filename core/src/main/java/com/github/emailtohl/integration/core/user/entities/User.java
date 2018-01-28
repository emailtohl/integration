package com.github.emailtohl.integration.core.user.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.search.annotations.Store;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.exception.InnerDataStateException;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.common.jpa.entity.EnumBridgeCust;
import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.user.UserType;
/**
 * 用户实体类
 * javax校验的注解在field上，JPA约束的注解写在JavaBean属性上
 * @author HeLei
 */
@org.hibernate.search.annotations.Indexed
@org.hibernate.envers.Audited
@Entity
@Table(name = "users")
@Access(AccessType.PROPERTY) // 实际上这就是默认的配置，Hibernate实现会根据@Id所在地方进行判断
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User extends BaseEntity {
	private static final long serialVersionUID = -2648409468140926726L;
	protected String name;
	protected String nickname;// 可存储第三方昵称
	@Pattern(// 校验
		regexp = ConstantPattern.EMAIL,
		flags = {Pattern.Flag.CASE_INSENSITIVE}
	)
	protected String email;
	
	@Pattern(regexp = ConstantPattern.CELL_PHONE)
	protected String cellPhone;
	
	protected String telephone;
	@Size(min = 5, message = "密码至少5位")
	@Pattern(regexp = "^[\\x21-\\x7e]*$", message = "用简单字符作为密码")
	protected transient String password;
	protected Boolean enabled = true;
	protected Boolean accountNonExpired = true;
	protected Boolean credentialsNonExpired = true;
	protected Boolean accountNonLocked = true;
	protected Date lastLogin; // 最后一次登录时间
	protected Date lastChangeCredentials; // 最后更改密码时间
	@Past // 校验，日期相对于当前较早
	protected Date birthday;
	@Min(value = 1)
	@Max(value = 120)
	protected Integer age;
	protected Gender gender;
	protected Image image;
	@Size(max = 300)
	protected String description;
	protected String publicKey;
	protected Set<Role> roles = new HashSet<Role>();
	
	// 用于时间范围内的搜索
	protected Date startDate;
	protected Date endDate;
	
	@org.hibernate.search.annotations.Field
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@org.hibernate.search.annotations.Field
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	@org.hibernate.search.annotations.Field
	@Column(unique = true, updatable = true)
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	@org.hibernate.search.annotations.Field
	@Column(name = "cell_phone", /*nullable = false, */unique = true, updatable = true)
	public String getCellPhone() {
		return cellPhone;
	}
	
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}
	
	@org.hibernate.search.annotations.Field
	@Column(unique = true)
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@org.hibernate.envers.NotAudited
	@Column(nullable = false)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@org.hibernate.envers.NotAudited
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	@org.hibernate.envers.NotAudited
	@Column(name = "account_non_expired")
	public Boolean getAccountNonExpired() {
		return accountNonExpired;
	}
	public void setAccountNonExpired(Boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}
	
	@org.hibernate.envers.NotAudited
	@Column(name = "credentials_non_expired")
	public Boolean getCredentialsNonExpired() {
		return credentialsNonExpired;
	}
	public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}
	
	@org.hibernate.envers.NotAudited
	@Column(name = "account_non_locked")
	public Boolean getAccountNonLocked() {
		return accountNonLocked;
	}
	public void setAccountNonLocked(Boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@org.hibernate.envers.NotAudited
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_login")
	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@org.hibernate.envers.NotAudited
	@Temporal(TemporalType.DATE)
	@Column(name = "last_change_credentials")
	public Date getLastChangeCredentials() {
		return lastChangeCredentials;
	}
	public void setLastChangeCredentials(Date lastChangeCredentials) {
		this.lastChangeCredentials = lastChangeCredentials;
	}
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
//	@org.hibernate.search.annotations.Field(index = org.hibernate.search.annotations.Index.YES, analyze=org.hibernate.search.annotations.Analyze.NO, store = org.hibernate.search.annotations.Store.YES)
//	@org.hibernate.search.annotations.DateBridge(resolution = org.hibernate.search.annotations.Resolution.DAY)
	@org.hibernate.envers.NotAudited
	@Temporal(TemporalType.DATE)
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@org.hibernate.envers.NotAudited
	public Integer getAge() {
		Integer age;
		if (this.birthday != null) {
//			java.sql.Date不支持toInstant方法
//			Instant timestamp = this.birthday.toInstant();
			Instant timestamp = Instant.ofEpochMilli(this.birthday.getTime());
			LocalDateTime date = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());
			LocalDate today = LocalDate.now();
			LocalDate pastDate = date.toLocalDate();
			Period years = Period.between(pastDate, today);
			age = years.getYears();
		} else {
			age = this.age;
		}
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	
	@org.hibernate.envers.NotAudited
	@org.hibernate.search.annotations.Field(bridge = @org.hibernate.search.annotations.FieldBridge(impl = EnumBridgeCust.class))
	// 枚举存入数据库默认为序号，这里指明将枚举名以字符串存入数据库
	@Enumerated(EnumType.STRING)
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	@org.hibernate.envers.NotAudited
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	
	@org.hibernate.envers.NotAudited
	@org.hibernate.search.annotations.Field(store = Store.COMPRESS)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@org.hibernate.envers.NotAudited
	@Lob
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	// 关联对象会自动被设为@Indexed
	@org.hibernate.search.annotations.IndexedEmbedded(depth = 1)
	@ManyToMany
	@JoinTable(name = "users_role"
	, joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") }
	, inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id") })
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	/**
	 * 由于多对多关系，可通过本方法直接获取该用户的授权
	 * @return
	 */
	public Set<String> authorityNames() {
		Set<String> names = new HashSet<String>();
		getRoles().forEach(r -> names.addAll(r.authorityNames()));
		return names;
	}
	
	@Transient
	public UserType getUserType() {
		if (this instanceof Employee) {
			return UserType.Employee;
		}
		if (this instanceof Customer) {
			return UserType.Customer;
		}
		if (this instanceof User) {
			return UserType.User;
		}
		throw new InnerDataStateException("不存在的用户类型");
	}
	public void setUserType(UserType userType) {}
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Transient
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Transient
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Set<String> roleNames() {
		return roles.stream().map(r -> r.getName()).collect(Collectors.toSet());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof User))
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		return true;
	}
	
}

