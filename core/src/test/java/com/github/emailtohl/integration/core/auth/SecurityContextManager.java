package com.github.emailtohl.integration.core.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.user.Constant;

/**
 * 用于测试的安全管理器
 * @author HeLei
 */
class SecurityContextManager {
	private static final Logger LOG = LogManager.getLogger();
	AuthenticationManager authenticationManager;
	final String customerDefaultPassword;
	final String employeeDefaultPassword;
	
	final CoreTestData td = new CoreTestData();
	
	public SecurityContextManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
		Properties prop = new Properties();
		try (InputStream in = SecurityContextManager.class.getResourceAsStream("/config.properties")) {
			prop.load(in);
			customerDefaultPassword = prop.getProperty(Constant.PROP_CUSTOMER_DEFAULT_PASSWORD).trim();
			employeeDefaultPassword = prop.getProperty(Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD).trim();
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
	}

	public void clearContext() {
		SecurityContextHolder.clearContext();
	}
	
	public void setEmailtohl() {
		SecurityContextHolder.clearContext();
		String name = td.user_emailtohl.getEmail();
		Authentication token = new UsernamePasswordAuthenticationToken(name, customerDefaultPassword);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOG.debug(authentication.getPrincipal());
	}
	
	public void setFoo() {
		SecurityContextHolder.clearContext();
		String name = td.foo.getEmpNum().toString();
		Authentication token = new UsernamePasswordAuthenticationToken(name, employeeDefaultPassword);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOG.debug(authentication.getPrincipal());
	}
	
	public void setBar() {
		SecurityContextHolder.clearContext();
		String name = td.bar.getEmpNum().toString();
		Authentication token = new UsernamePasswordAuthenticationToken(name, employeeDefaultPassword);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOG.debug(authentication.getPrincipal());
	}
	
	public void setBaz() {
		SecurityContextHolder.clearContext();
		String name = td.baz.getCellPhone();
		Authentication token = new UsernamePasswordAuthenticationToken(name, customerDefaultPassword);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOG.debug(authentication.getPrincipal());
	}

	public void setQux() {
		SecurityContextHolder.clearContext();
		String name = td.qux.getEmail();
		Authentication token = new UsernamePasswordAuthenticationToken(name, customerDefaultPassword);
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		LOG.debug(authentication.getPrincipal());
	}
}
