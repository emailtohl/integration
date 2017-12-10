package com.github.emailtohl.integration.core.auth;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * org.springframework.security.authentication.AuthenticationManager接口需要返回的实例
 * @author HeLei
 */
public class AuthenticationImpl implements Authentication {
	private static final long serialVersionUID = 2055148436391599815L;
	/**
	 * Gson序列化对象是根据对象的Field字段而不是根据JavaBean属性，所以这里还需有Field字段
	 */
	private String name;
	private String password;
	@SuppressWarnings("unused")
	private Set<String> authorities;
	private Details details;
	private UserDetails principal;
	private boolean authenticated;
	
	public AuthenticationImpl(UserDetails principal) {
		super();
		this.name = principal.getUsername();
		this.password = principal.getPassword();
		this.authorities = AuthorityUtils.authorityListToSet(principal.getAuthorities());
		this.principal = principal;
	}

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
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
		if (this.principal instanceof UserDetailsImpl) {
			((UserDetailsImpl) this.principal).eraseCredentials();
		}
	}

	/**
	 * Stores additional details about the authentication request.
	 * These might be an IP address, certificate serial number etc.
	 */
	@Override
	public Object getDetails() {
		return this.details;
	}
	public void setDetails(Details details) {
		this.details = details;
	}

	/**
	 * The identity of the principal being authenticated. In the
	 * case of an authentication request with username and password,
	 * this would be the username. Callers are expected to populate
	 * the principal for an authentication request.
	 * 按照描述，getPrincipal()返回的应该是某种形式的用户名
	 * 但是spring security需要在这个返回中获取更多的用户信息，结构是
	 * org.springframework.security.core.userdetails.UserDetails
	 */
	@Override
	public Object getPrincipal() {
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

    /**
     * Returns a string representation of this principal.
     *
     * @return a string representation of this principal.
     */
	@Override
	public String toString() {
		return this.principal.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

    /**
     * Compares this principal to the specified object.  Returns true
     * if the object passed in matches the principal represented by
     * the implementation of this interface.
     * 
     * principal的唯一标识是username，在这里就是name
     *
     * @param another principal to compare with.
     *
     * @return true if the principal passed in is the same as that
     * encapsulated by this principal, and false otherwise.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuthenticationImpl other = (AuthenticationImpl) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
