package com.github.emailtohl.integration.core.user.auth;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
	private Object details;
	private UserDetails principal;
	private boolean authenticated;
	
	public AuthenticationImpl() {
	}
	
	public AuthenticationImpl(String name, String password, Set<String> authorities, Object details,
			UserDetails principal, boolean authenticated) {
		super();
		this.name = name;
		this.password = password;
		this.authorities = authorities;
		this.details = details;
		this.principal = principal;
		this.authenticated = authenticated;
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
		if (this.principal instanceof UserDetailsImpl) {
			((UserDetailsImpl) this.principal).eraseCredentials();
		}
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
