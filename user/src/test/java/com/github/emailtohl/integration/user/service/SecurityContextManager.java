package com.github.emailtohl.integration.user.service;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.emailtohl.integration.user.UserTestData;
/**
 * 依赖于AuthenticationManager
 * 为测试环境提供已认证的用户
 * @author HeLei
 * @date 2017.02.04
 */
@Component
public class SecurityContextManager {
	private static final Logger logger = LogManager.getLogger();
	private AuthenticationManager authenticationManager;
	private final String password = "123456";
	final UserTestData td = new UserTestData();
	
	/**
	 * 注入的AuthenticationManager就是
	 * com.github.emailtohl.integration.user.service.AuthenticationManagerImpl
	 */
	@Inject
	public SecurityContextManager(AuthenticationManager authenticationManager) {
		super();
		this.authenticationManager = authenticationManager;
	}
	
	public void clearContext() {
		SecurityContextHolder.clearContext();
	}
	
	public void setEmailtohl() {
		SecurityContextHolder.clearContext();
		String name = td.emailtohl.getEmail();
		Authentication token = new UsernamePasswordAuthenticationToken(name, password);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		logger.debug(authentication.getPrincipal());
	}
	
	public void setFoo() {
		SecurityContextHolder.clearContext();
		String name = td.foo.getEmail();
		Authentication token = new UsernamePasswordAuthenticationToken(name, password);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		logger.debug(authentication.getPrincipal());
	}
	
	public void setBar() {
		SecurityContextHolder.clearContext();
		String name = td.bar.getEmail();
		Authentication token = new UsernamePasswordAuthenticationToken(name, password);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		logger.debug(authentication.getPrincipal());
	}
	
	public void setBaz() {
		SecurityContextHolder.clearContext();
		String name = td.baz.getEmail();
		Authentication token = new UsernamePasswordAuthenticationToken(name, password);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		logger.debug(authentication.getPrincipal());
	}
}
