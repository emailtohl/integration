package com.github.emailtohl.integration.core.auth;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.emailtohl.integration.core.user.entities.Classify;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * org.springframework.security.core.userdetails.UserDetailsService接口需要返回的实例
 * 在SpEL表达式中authentication.principal引用的是AuthenticationImpl.getPrincipal()
 * 而AuthenticationImpl中的principal是本类实例
 * 
 * @author HeLei
 */
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1635134127318665555L;

	/**
	 * Gson序列化对象是根据对象的Field字段而不是根据JavaBean属性，所以这里还需有Field字段
	 */
	public final String username;
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
	private String iconUrl;// 额外携带用户图片信息
	private Boolean accountNonLocked;
	private Boolean accountNonExpired;
	private Boolean credentialsNonExpired;
	
	public UserDetailsImpl(User u) {
		this.id = u.getId();
		this.realName = u.getName();
		this.nickname = u.getNickname();
		this.cellPhone = u.getCellPhone();
		this.email = u.getEmail();
		if (u instanceof Employee) {
			this.userType = UserType.Employee;
			this.empNum = ((Employee) u).getEmpNum();
		} else if (u instanceof Customer) {
			this.userType = UserType.Customer;
			this.level = ((Customer) u).getLevel();
			this.classify = ((Customer) u).getClassify();
		}
		this.authorities = u.authorityNames();
		this.password = u.getPassword();
		if (u.getImage() != null) {
			this.iconUrl = u.getImage().getPath();
		}
		this.accountNonExpired = u.getAccountNonExpired();
		this.accountNonLocked = u.getAccountNonLocked();
		this.credentialsNonExpired = u.getCredentialsNonExpired();
		this.username = UniqueUsername.get(u);
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

	public String getIconUrl() {
		return this.iconUrl;
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
		return "UserDetailsImpl [username=" + username + ", id=" + id + ", realName=" + realName + ", userType="
				+ userType + ", nickname=" + nickname + ", cellPhone=" + cellPhone + ", email=" + email + ", empNum="
				+ empNum + ", level=" + level + ", classify=" + classify + ", authorities=" + authorities
				+ ", password=" + password + ", iconUrl=" + iconUrl + ", accountNonLocked=" + accountNonLocked
				+ ", accountNonExpired=" + accountNonExpired + ", credentialsNonExpired=" + credentialsNonExpired + "]";
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
