package com.github.emailtohl.integration.core.auth;

import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.core.Authentication;

/**
 * 登出相关的事件
 * @author HeLei
 */
public class LogoutEvent extends AbstractAuthenticationEvent {
	private static final long serialVersionUID = 4677549694759529069L;

	public LogoutEvent(Authentication authentication) {
		super(authentication);
	}

}
