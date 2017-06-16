package com.github.emailtohl.integration.user.security;

import java.io.Serializable;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.encryption.myrsa.Encipher;
import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.user.entities.User;
import com.github.emailtohl.integration.user.service.UserService;

/**
 * 本类实现了AuthenticationManager
 * @author HeLei
 * @date 2017.06.15
 */
@Service
public class AuthenticationManagerImpl implements AuthenticationManager {
	private static final transient Logger logger = LogManager.getLogger();
	private UserService userService;
	private Encipher encipher = new Encipher();
	private String privateKey;
	
	@Inject
	public AuthenticationManagerImpl(UserService userService) {
		super();
		this.userService = userService;
	}

	/**
	 * 下面是实现AuthenticationProvider，可以供Spring Security框架使用
	 */
	@Transactional
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UsernamePasswordAuthenticationToken credentials = (UsernamePasswordAuthenticationToken) authentication;
		String email = credentials.getPrincipal().toString();
		String password = credentials.getCredentials().toString();
		// 用户名和密码用完后，记得擦除
		credentials.eraseCredentials();
		return authenticate(email, password);
	}
	
	public Authentication authenticate(String email, String password) throws AuthenticationException {
		User u = null;
		try {
			u = userService.getUserByEmail(email);
		} catch (ResourceNotFoundException e) {
			logger.warn("Authentication failed for non-existent user {}.", email);
			throw new UsernameNotFoundException("没有此用户");
		}
		String userPassword;
		if (privateKey != null) {
			userPassword = encipher.decrypt(password, privateKey);
		} else {
			userPassword = password;
		}
		if (!BCrypt.checkpw(userPassword, u.getPassword())) {
			logger.warn("Authentication failed for user {}.", email);
			throw new BadCredentialsException("密码错误");
		}
		logger.debug("User {} successfully authenticated.", email);

		Details d = new Details();
		// ...
		UserDetails principal = new UserDetailsImpl(u);
		
		AuthenticationImpl a = new AuthenticationImpl(u.getEmail(), u.getPassword(), u.authorities(), d, principal, true);
		// 构造器已经设置完成，为了表达逻辑，所以下面三条语句冗余
		a.setAuthenticated(true);
		a.eraseCredentials();
		a.setDetails(d);
		return a;
	}
	
	public class Details implements Serializable {
		private static final long serialVersionUID = -7461854984848054398L;
		String remoteAddress;
		String sessionId;
		String certificateSerialNumber;
		public String getRemoteAddress() {
			return remoteAddress;
		}
		public void setRemoteAddress(String remoteAddress) {
			this.remoteAddress = remoteAddress;
		}
		public String getSessionId() {
			return sessionId;
		}
		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}
		public String getCertificateSerialNumber() {
			return certificateSerialNumber;
		}
		public void setCertificateSerialNumber(String certificateSerialNumber) {
			this.certificateSerialNumber = certificateSerialNumber;
		}
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
}
