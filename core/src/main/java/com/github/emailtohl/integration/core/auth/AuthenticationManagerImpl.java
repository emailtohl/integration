package com.github.emailtohl.integration.core.auth;

import java.util.Date;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.common.encryption.myrsa.Encipher;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 本类实现了AuthenticationManager
 * @author HeLei
 */
//@Service
//@Named("authenticationManager")
public class AuthenticationManagerImpl implements AuthenticationManager {
	protected static final transient Logger LOG = LogManager.getLogger();
	
	@Inject
	protected UserService userService;
	@Inject
	protected ApplicationEventPublisher publisher;
	protected Encipher encipher = new Encipher();
	protected String privateKey;

	public AuthenticationManagerImpl() {}
	
	public AuthenticationManagerImpl(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 下面是实现AuthenticationProvider，可以供Spring Security框架使用
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UsernamePasswordAuthenticationToken credentials = (UsernamePasswordAuthenticationToken) authentication;
		String principal = credentials.getPrincipal().toString();
		String password = credentials.getCredentials().toString();
		// 用户名和密码用完后，记得擦除
		credentials.eraseCredentials();
		return authenticate(principal, password);
	}

	/**
	 * @param username 平台账号的empNum，客户账号的手机号或邮箱，不能使用自动审批账号和匿名账号登录
	 * @param password
	 * @return
	 * @throws AuthenticationException
	 */
	public Authentication authenticate(String username, String password) throws AuthenticationException {
		User u = userService.findAndRefreshLastLogin(username);
		if (u == null) {
			LOG.warn("Authentication failed for non-existent user {}.", username);
			throw new UsernameNotFoundException("没有此账号");
		}
		
		if (u instanceof Employee && Employee.NO_BOT == ((Employee) u).getEmpNum()) {
			LOG.warn("Authentication Don't login this account {}.", username);
			throw new UsernameNotFoundException("没有此账号");
		}
		if (u instanceof Customer && Constant.ANONYMOUS_EMAIL.equals(u.getEmail())) {
			LOG.warn("Authentication Don't login this account {}.", username);
			throw new UsernameNotFoundException("没有此账号");
		}
		
		if (u.getEnabled() != null && !u.getEnabled()) {
			LOG.warn("Authentication enable for user {}.", username);
			throw new DisabledException("账号未启用");
		}
		if (u.getAccountNonLocked() != null && !u.getAccountNonLocked()) {
			LOG.warn("Authentication locked for user {}.", username);
			throw new LockedException("账号被锁住");
		}
		if (u.getAccountNonExpired() != null && !u.getAccountNonExpired()) {
			LOG.warn("Authentication account expired for user {}.", username);
			throw new AccountExpiredException("账号已过期");
		}
		if (u.getCredentialsNonExpired() != null && !u.getCredentialsNonExpired()) {
			LOG.warn("Authentication credentials expired for user {}.", username);
			throw new CredentialsExpiredException("密码过期");
		}
		String userPassword;
		if (this.privateKey != null) {
			userPassword = encipher.decrypt(password, this.privateKey);
		} else {
			userPassword = password;
		}
		if (!BCrypt.checkpw(userPassword, u.getPassword())) {
			LOG.warn("Authentication failed for user {}.", username);
			throw new BadCredentialsException("密码错误");
		}
		LOG.debug("User {} successfully authenticated.", username);

		UserDetails principal = new UserDetailsImpl(u, username/*用登录时的用户名*/);

		AuthenticationImpl a = new AuthenticationImpl(principal);
		a.setAuthenticated(true);
		a.eraseCredentials();
		a.setDetails(getDetails());
		if (publisher != null) {
			publisher.publishEvent(new LoginEvent(a));
		}
		u.setLastLogin(new Date());
		return a;
	}

	public Encipher getEncipher() {
		return encipher;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	private Details getDetails() {
		Details d = new Details();
		d.setSessionId(ThreadContext.get(Constant.SESSION_ID_PROPERTY_NAME));
		d.setRemoteAddress(ThreadContext.get(Constant.REMOTE_ADDRESS_PROPERTY_NAME));
		return d;
	}
}
