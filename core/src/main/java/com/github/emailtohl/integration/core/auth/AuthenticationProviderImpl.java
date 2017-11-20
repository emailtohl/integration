package com.github.emailtohl.integration.core.auth;

import javax.inject.Named;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.core.user.service.UserService;

/**
 * 本类实现了AuthenticationProvider，它是一个AuthenticationManager，可被外部使用
 * 用于spring security配置AuthenticationManagerBuilder中
 * @author HeLei
 */
@Service
@Named("authenticationProvider")
public class AuthenticationProviderImpl extends AuthenticationManagerImpl implements AuthenticationProvider {

	public AuthenticationProviderImpl() {
		super();
	}

	public AuthenticationProviderImpl(UserService userService) {
		super(userService);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication == UsernamePasswordAuthenticationToken.class;
	}

}
