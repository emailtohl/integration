package com.github.emailtohl.integration.core.user.auth;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

/**
 * 登出实现
 * @author HeLei
 */
@Service
@Named("logoutHandler")
public class LogoutHandlerImpl implements LogoutHandler {
	SecurityContextLogoutHandler hander = new SecurityContextLogoutHandler();

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		hander.logout(request, response, authentication);
	}

}
