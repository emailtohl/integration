package com.github.emailtohl.integration.user.security;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.emailtohl.integration.user.entities.User;

/**
 * org.springframework.security.core.userdetails.UserDetailsService接口需要返回的实例
 * @author HeLei
 * @date 2017.06.16
 */
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1635134127318665555L;

	/**
	 * Gson序列化对象是根据对象的Field字段而不是根据JavaBean属性，所以这里还需有Field字段
	 */
	private Long id;
	private String username;
	private String email;
	private Set<String> authorities;
	private String password;
	private String iconSrc;// 额外携带用户图片信息
	private Boolean accountNonExpired;
	private Boolean accountNonLocked;
	private Boolean credentialsNonExpired;
	private Boolean enabled;
	
	public UserDetailsImpl() {
	}

	public UserDetailsImpl(Long id, String username, String email, Set<String> authorities, String password,
			String iconSrc, Boolean accountNonExpired, Boolean accountNonLocked, Boolean credentialsNonExpired,
			Boolean enabled) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
		this.authorities = authorities;
		this.password = password;
		this.iconSrc = iconSrc;
		this.accountNonExpired = accountNonExpired;
		this.accountNonLocked = accountNonLocked;
		this.credentialsNonExpired = credentialsNonExpired;
		this.enabled = enabled;
	}

	public UserDetailsImpl(User u) {
		this.id = u.getId();
		this.username = u.getUsername();
		this.email = u.getEmail();
		this.authorities = u.authorities();
		this.password = u.getPassword();
		this.iconSrc = u.getIconSrc();
		this.accountNonExpired = u.getAccountNonExpired();
		this.accountNonLocked = u.getAccountNonLocked();
		this.credentialsNonExpired = u.getCredentialsNonExpired();
		this.enabled = u.getEnabled();
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

	// 认证的时候存储密码，用过之后会擦除
	public void eraseCredentials() {
		this.password = null;
	}
	
	@Override
	public String toString() {
		return "UserDetailsImpl [id=" + id + ", username=" + username + ", email=" + email + ", authorities="
				+ authorities + ", iconSrc=" + iconSrc + ", accountNonExpired=" + accountNonExpired
				+ ", accountNonLocked=" + accountNonLocked + ", credentialsNonExpired=" + credentialsNonExpired
				+ ", enabled=" + enabled + "]";
	}

}
