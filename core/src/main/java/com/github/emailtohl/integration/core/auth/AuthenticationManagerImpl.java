package com.github.emailtohl.integration.core.auth;

import java.io.Serializable;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.encryption.myrsa.Encipher;
import com.github.emailtohl.integration.common.exception.InnerDataStateException;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.integration.core.user.service.UserService;

/**
 * 本类实现了AuthenticationManager
 * @author HeLei
 */
//@Service
//@Named("authenticationManager")
public class AuthenticationManagerImpl implements AuthenticationManager {
	protected static final transient Logger LOG = LogManager.getLogger();
	
	public static final String EMP_NUM_PREFIX = "emp_num:";
	public static final String CELL_PHONE_PREFIX = "cell_phone:";
	public static final String EMAIL_PREFIX = "email:";
	public static final String ID_PREFIX = "id:";
	
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

	public Authentication authenticate(String username, String password) throws AuthenticationException {
		User u = userService.find(username);
		if (u == null) {
			LOG.warn("Authentication failed for non-existent user {}.", username);
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

		// 这是辅助的一些信息
		Details d = new Details();
		// ...
		UserDetails principal = new UserDetailsImpl(u);

		AuthenticationImpl a = new AuthenticationImpl(getUniqueName(u), u.getPassword(), u.authorityNames(), d,
				principal, true);
		// 构造器已经设置完成，为了表达逻辑，所以下面三条语句冗余
		a.setAuthenticated(true);
		a.eraseCredentials();
		a.setDetails(d);
		if (publisher != null) {
			publisher.publishEvent(new LoginEvent(a));
		}
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

	public Encipher getEncipher() {
		return encipher;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	/**
	 * 在安全上下文中获取全局唯一识别的用户名
	 * @param u
	 * @return
	 */
	private String getUniqueName(User u) {
		StringBuilder uniqueName = new StringBuilder();
		if (u instanceof Employee) {
			uniqueName.append(EMP_NUM_PREFIX).append(((Employee) u).getEmpNum().toString());
		} else if (u instanceof Customer) {
			if (StringUtils.hasText(u.getCellPhone())) {
				uniqueName.append(CELL_PHONE_PREFIX).append(u.getCellPhone());
			}
			if (StringUtils.hasText(u.getEmail())) {
				if (uniqueName.length() > 0) {
					uniqueName.append(';');
				}
				uniqueName.append(EMAIL_PREFIX).append(u.getEmail());
			}
		} else if (u.getId() != null){
			uniqueName.append(ID_PREFIX).append(u.getId().toString());
		}
		if (uniqueName.length() == 0) {
			throw new InnerDataStateException("未获取到用户唯一标识");
		}
		return uniqueName.toString();
	}
}
