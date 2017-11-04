package com.github.emailtohl.integration.nuser.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * 本类实现了AuthenticationProvider，它是一个AuthenticationManager，可被外部使用
 * 用于spring security配置AuthenticationManagerBuilder中
 * @author HeLei
 */
@Service
public class AuthenticationProviderImpl extends AuthenticationManagerImpl implements AuthenticationProvider {

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication == UsernamePasswordAuthenticationToken.class;
	}

}
