package com.github.emailtohl.integration.nuser.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * 本类实现了AuthenticationManager
 * @author HeLei
 * @date 2017.06.15
 */
@Service
public class AuthenticationProviderImpl extends AuthenticationManagerImpl implements AuthenticationProvider {

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication == UsernamePasswordAuthenticationToken.class;
	}

}
