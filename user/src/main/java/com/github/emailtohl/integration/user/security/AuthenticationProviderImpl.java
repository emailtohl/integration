package com.github.emailtohl.integration.user.security;

import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.user.service.UserService;

/**
 * 本类实现了AuthenticationManager
 * @author HeLei
 * @date 2017.06.15
 */
@Service
public class AuthenticationProviderImpl extends AuthenticationManagerImpl implements AuthenticationProvider {

	@Inject
	public AuthenticationProviderImpl(UserService userService) {
		super(userService);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication == UsernamePasswordAuthenticationToken.class;
	}

}
