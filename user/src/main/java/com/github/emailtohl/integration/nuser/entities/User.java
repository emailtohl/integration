package com.github.emailtohl.integration.nuser.entities;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.github.emailtohl.integration.common.Constant;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
/**
 * 用户实体类
 * javax校验的注解在field上，JPA约束的注解写在JavaBean属性上
 * @author HeLei
 * @date 2017.02.04
 */
@org.hibernate.envers.Audited
@Entity
//@Table(name = "users")
@Access(AccessType.PROPERTY) // 实际上这就是默认的配置，Hibernate实现会根据@Id所在地方进行判断
//考虑到不同用户的属性要求不一样，例如顾客必填项是电话号码，后台用户必填项是username，所以继承策略为不同用户不同表
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User extends BaseEntity {
	private static final long serialVersionUID = -2648409468140926726L;
	public enum Gender {
		MALE, FEMALE, UNSPECIFIED
	}
	protected String name;
	protected String username;
	@Pattern(// 校验
		regexp = Constant.PATTERN_EMAIL,
		flags = {Pattern.Flag.CASE_INSENSITIVE}
	)
	protected String email;
	protected String telephone;
	@Size(min = 6)
	@Pattern(regexp = "^[^\\s&\"<>]+$")
	protected transient String password;
	protected Boolean accountNonExpired = true;
	protected Boolean credentialsNonExpired = true;
	protected Boolean accountNonLocked = true;
	@Past// 校验，日期相对于当前较早
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
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@org.hibernate.search.annotations.Field
	@Column(nullable = false, unique = true)
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
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
	
	@org.hibernate.envers.NotAudited
	@Column(nullable = false)
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
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
	
	// 枚举存入数据库默认为序号，这里指明将枚举名以字符串存入数据库
	@Enumerated(EnumType.STRING)
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	
	@org.hibernate.search.annotations.Field
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Lob
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	@ManyToMany
	@JoinTable(name = "t_user_role"
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
	public Set<String> authorities() {
		Set<Authority> set = new HashSet<Authority>();
		roles.forEach(r -> set.addAll(r.getAuthorities()));
		return set.stream().map(a -> a.getName()).collect(Collectors.toSet());
	}
	
	@Transient
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	@Transient
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
}
