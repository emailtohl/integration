package com.github.emailtohl.integration.core.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;

/**
 * 用于测试的安全管理器
 * @author HeLei
 */
class SecurityContextManager {
	private static final Logger LOG = LogManager.getLogger();
	AuthenticationManager authenticationManager;
	private final String password = "123456";
	final CoreTestData td = new CoreTestData();
	
	public SecurityContextManager(AuthenticationManager authenticationManager) {
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
		LOG.debug(authentication.getPrincipal());
	}
	
	public void setFoo() {
		SecurityContextHolder.clearContext();
		String name = td.foo.getEmpNum().toString();
		Authentication token = new UsernamePasswordAuthenticationToken(name, password);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOG.debug(authentication.getPrincipal());
	}
	
	public void setBar() {
		SecurityContextHolder.clearContext();
		String name = td.bar.getEmpNum().toString();
		Authentication token = new UsernamePasswordAuthenticationToken(name, password);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOG.debug(authentication.getPrincipal());
	}
	
	public void setBaz() {
		SecurityContextHolder.clearContext();
		String name = td.baz.getCellPhone();
		Authentication token = new UsernamePasswordAuthenticationToken(name, password);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOG.debug(authentication.getPrincipal());
	}
}
