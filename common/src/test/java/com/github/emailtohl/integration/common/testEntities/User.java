package com.github.emailtohl.integration.common.testEntities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
/**
 * 用户实体类
 * javax校验的注解在field上，JPA约束的注解写在JavaBean属性上
 * @author HeLei
 */
@org.hibernate.envers.Audited
@Entity
@Table(name = "t_user")
@Access(AccessType.PROPERTY) // 实际上这就是默认的配置，Hibernate实现会根据@Id所在地方进行判断
//指定继承的映射策略，所有继承树上的实体共用一张表：SINGLE_TABLE，这是默认值
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//定义辨别者列的列名为“user_type”，列类型是字符串
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
//指定User实体对应的记录在辨别者列的值是“user”
@DiscriminatorValue("user") // 若不注解，则默认使用实体名
public class User extends BaseEntity {
	private static final long serialVersionUID = -2648409468140926726L;
	public enum Gender {
		MALE, FEMALE, UNSPECIFIED
	}
	protected String name;
	protected String username;
	@NotNull// 校验
	@Pattern(// 校验
		regexp = ConstantPattern.EMAIL,
		flags = {Pattern.Flag.CASE_INSENSITIVE}
	)
	protected String email;
	protected String address;
	protected String telephone;
	@Size(min = 6)
	@Pattern(regexp = "^[^\\s&\"<>]+$")
	protected transient String password;
	protected Boolean enabled;
	protected Boolean accountNonExpired = true;
	protected Boolean credentialsNonExpired = true;
	protected Boolean accountNonLocked = true;
	@Past// 校验，日期相对于当前较早
	protected Date birthday;
	@Min(value = 1)
	@Max(value = 120)
	protected Integer age;
	protected Gender gender;
	@Valid
	protected Subsidiary subsidiary;
	@Size(max = 1048576)
	protected transient byte[] icon;
	protected String iconSrc;
	@Size(max = 300)
	protected String description;
	protected String publicKey;
//	protected Set<Authority> authorities = new HashSet<Authority>();
	protected Set<Role> roles = new HashSet<Role>();
	
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
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
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
	
	@org.hibernate.envers.NotAudited
	@Embedded
	/*嵌入属性的映射可在嵌入实体中声明，不必要在此覆盖
	@AttributeOverrides({
		@AttributeOverride(name = "city", column = @Column(name = "subsidiary_city")),
		@AttributeOverride(name = "province", column = @Column(name = "subsidiary_province"))
	})*/
	public Subsidiary getSubsidiary() {
		return subsidiary;
	}
	public void setSubsidiary(Subsidiary subsidiary) {
		this.subsidiary = subsidiary;
	}
	
	@org.hibernate.envers.NotAudited
	@Lob
	@Basic(fetch = FetchType.LAZY)
	public byte[] getIcon() {
		return icon;
	}
	public void setIcon(byte[] icon) {
		this.icon = icon;
	}
	
	@Column(name = "icon_src")
	public String getIconSrc() {
		return iconSrc;
	}
	public void setIconSrc(String iconSrc) {
		this.iconSrc = iconSrc;
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
	
/*	
	// 此属性暂时为目前配置的spring security提供服务
	@ElementCollection(targetClass = Authority.class, fetch = FetchType.EAGER)
	// @CollectionTable是可选项，若不做注解，JPA提供者则会根据自动生成连接表的表名以及对应的列名
	@CollectionTable(name = "t_user_authority"
	, joinColumns = {
			// 注意，这里定义的是对主键的引用，非主键不能写在这里，否则查询会异常
			@JoinColumn(name = "user_id", referencedColumnName = "id")
		})
	@Enumerated(EnumType.STRING)// 若不指定此项，数据表中默认存储枚举的序号
	@Column(name = "authority")// 若不加此项，连接表中默认为authorities
	public Set<Authority> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(Set<Authority> authorities) {
		this.authorities = authorities;
	}
	*/
	@ManyToMany(targetEntity = Role.class, fetch = FetchType.EAGER)
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
	public AuthenticationImpl getAuthentication() {
		return new AuthenticationImpl();
	}
	
	@Transient
	public Principal getUserDetails() {
		return new Principal();
	}
	
	/**
	 * 基于唯一标识email的equals和hashCode方法
	 */
	@Override
	public int hashCode() {
		return Objects.hash(email);
	}
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof User))
			return false;
		final User that = (User) other;
		if (this.email == null || that.getEmail() == null)
			return false;
		else
			return this.email.equals(that.getEmail());
	}
	
	@Override
	public String toString() {
		return "User [name=" + name + ", username=" + username + ", email=" + email + ", address=" + address
				+ ", telephone=" + telephone + ", enabled=" + enabled + ", accountNonExpired=" + accountNonExpired
				+ ", credentialsNonExpired=" + credentialsNonExpired + ", accountNonLocked=" + accountNonLocked
				+ ", birthday=" + birthday + ", age=" + age + ", gender=" + gender + ", subsidiary=" + subsidiary
				+ ", iconSrc=" + iconSrc + ", description=" + description + ", roles=" + roles + "]";
	}

	/**
	 * 下面是获取Authentication和UserDetails的方法
	 * 本类并没有直接实现Authentication和UserDetails的原因是考虑传输到前台的认证信息不需要过多携带User类中的信息
	 */
	public class Principal implements UserDetails {
		private static final long serialVersionUID = -2808344559121367648L;
		/**
		 * Gson序列化对象是根据对象的Field字段而不是根据JavaBean属性，所以这里还需有Field字段
		 */
		Long id;
		String username;
		String email;
		Set<String> authorities;
		String password;
		String iconSrc;// 额外携带用户图片信息
		
		public Principal() {
			this.id = User.this.id == null ? null : new Long(User.this.id);
			this.username = User.this.username == null ? null : new String(User.this.username);
			this.email = User.this.email == null ? null : new String(User.this.email);
			this.password = User.this.password == null ? null : new String(User.this.password);
			this.authorities = User.this.authorities();
			this.iconSrc = User.this.iconSrc;
		}
		
		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return AuthorityUtils.createAuthorityList(this.authorities.toArray(new String[this.authorities.size()]));
		}

		@Override
		public String getPassword() {
			return this.password;
		}

		@Override
		public String getUsername() {
			return this.username;
		}
		
		public Long getId() {
			return this.id;
		}

		public String getIconSrc() {
			return this.iconSrc;
		}
		
		public String getEmail() {
			return this.email;
		}
		
		@Override
		public boolean isAccountNonExpired() {
			return accountNonExpired == null ? false : accountNonExpired;
		}

		@Override
		public boolean isAccountNonLocked() {
			return accountNonLocked == null ? false : accountNonLocked;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return credentialsNonExpired == null ? false : credentialsNonExpired;
		}

		@Override
		public boolean isEnabled() {
			return enabled == null ? false : enabled;
		}

		@Override
		public String toString() {
			return "Principal [id=" + id + ", username=" + username + ", email=" + email + ", authorities="
					+ authorities + "]";
		}
	}
	
	public class AuthenticationImpl implements Authentication {
		private static final long serialVersionUID = -1446199832307837361L;
		/**
		 * Gson序列化对象是根据对象的Field字段而不是根据JavaBean属性，所以这里还需有Field字段
		 */
		String name;
		String password;
		Set<String> authorities;
		Object details;
		Principal principal;
		boolean authenticated;
		
		public AuthenticationImpl() {
			this.principal = new Principal();
			this.name = principal.username;
			this.password = principal.password;
			this.authorities = principal.authorities;
		}
		
		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return principal.getAuthorities();
		}

		@Override
		public Object getCredentials() {
			return this.password;
		}
		
		// 认证的时候存储密码，用过之后会擦除
		public void eraseCredentials() {
			this.password = null;
			this.principal.password = null;
		}

		@Override
		public Object getDetails() {
			/*
			 * Stores additional details about the authentication request.
			 * These might be an IP address, certificate serial number etc.
			 */
			return this.details;
		}
		public void setDetails(Object details) {
			this.details = details;
		}

		@Override
		public Object getPrincipal() {
			/*
			 * The identity of the principal being authenticated. In the
			 * case of an authentication request with username and password,
			 * this would be the username. Callers are expected to populate
			 * the principal for an authentication request.
			 * 按照描述，getPrincipal()返回的应该是某种形式的用户名
			 * 但是spring security需要在这个返回中获取更多的用户信息，结构是
			 * org.springframework.security.core.userdetails.UserDetails
			 */
			return this.principal;
		}

		@Override
		public boolean isAuthenticated() {
			return authenticated;
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			this.authenticated = isAuthenticated;
		}
		
	}

}
