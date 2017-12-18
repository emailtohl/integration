package com.github.emailtohl.integration.core.auth;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.user.UserType;
import com.github.emailtohl.integration.core.user.entities.Classify;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * org.springframework.security.core.userdetails.UserDetailsService接口需要返回的实例
 * 在SpEL表达式中，authentication即AuthenticationImpl的实例
 * authentication.principal就是UserDetailsImpl的实例
 * 
 * @author HeLei
 */
@SuppressWarnings("unused")
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1635134127318665555L;

	/**
	 * Gson序列化对象是根据对象的Field字段而不是根据JavaBean属性，所以这里还需有Field字段
	 */
	private String username = "anonymous";
	private Long id;
	private String realName;
	private UserType userType;
	private String nickname;
	private String cellPhone;
	private String email;
	private Integer empNum; // 跟平台账号相关
	private Customer.Level level; // 跟客户账号相关
	private Classify classify; // 跟客户账号相关
	private Set<String> authorities;
	private String password;
	private String iconSrc;// 额外携带用户图片信息
	private Boolean accountNonLocked;
	private Boolean accountNonExpired;
	private Boolean credentialsNonExpired;
	
	public UserDetailsImpl(User u) {
		if (u == null) {
			return;
		}
		this.id = u.getId();
		this.realName = u.getName();
		this.nickname = u.getNickname();
		this.cellPhone = u.getCellPhone();
		this.email = u.getEmail();
		if (u instanceof Employee) {
			this.userType = UserType.Employee;
			this.empNum = ((Employee) u).getEmpNum();
			if (this.empNum != null) {
				this.username = this.empNum.toString();
			}
		} else if (u instanceof Customer) {
			this.userType = UserType.Customer;
			this.level = ((Customer) u).getLevel();
			this.classify = ((Customer) u).getClassify();
			if (StringUtils.hasText(u.getEmail())) {
				this.username = u.getEmail();
			} else if (StringUtils.hasText(u.getCellPhone())) {
				this.username = u.getCellPhone();
			}
		} else if (u instanceof User && Constant.ADMIN_NAME.equals(u.getName())) {
			this.username = Constant.ADMIN_NAME;
		}
		this.authorities = u.authorityNames();
		this.password = u.getPassword();
		if (u.getImage() != null) {
			this.iconSrc = u.getImage().getSrc();
		}
		this.accountNonExpired = u.getAccountNonExpired();
		this.accountNonLocked = u.getAccountNonLocked();
		this.credentialsNonExpired = u.getCredentialsNonExpired();
	}
	
	/**
	 * 指定username
	 * @param u
	 * @param username
	 */
	public UserDetailsImpl(User u, String username) {
		this(u);
		if (!StringUtils.hasText(username)) {
			throw new IllegalArgumentException("the username never null");
		}
		this.username = username;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList(this.authorities.toArray(new String[this.authorities.size()]));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	/**
	 * Returns the username used to authenticate the user. Cannot return <code>null</code>
	 *
	 * @return the username (never <code>null</code>)
	 */
	@Override
	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String username) {
		if (!StringUtils.hasText(username)) {
			throw new IllegalArgumentException("the username never null");
		}
		this.username = username;
	}
	
	public Long getId() {
		return this.id;
	}

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public Integer getEmpNum() {
		return empNum;
	}

	public void setEmpNum(Integer empNum) {
		this.empNum = empNum;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIconSrc() {
		return this.iconSrc;
	}
	
	public String getRealName() {
		return this.realName;
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
		return accountNonLocked == null ? false : accountNonLocked;
	}

	// 认证的时候存储密码，用过之后会擦除
	public void eraseCredentials() {
		this.password = null;
	}

	@Override
	public String toString() {
		return "UserDetailsImpl [username=" + username + ", authorities=" + authorities + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		UserDetailsImpl other = (UserDetailsImpl) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
