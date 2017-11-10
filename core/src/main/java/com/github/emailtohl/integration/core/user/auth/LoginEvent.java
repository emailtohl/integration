package com.github.emailtohl.integration.core.user.auth;

import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.core.Authentication;

/**
 * 登录相关的事件
 * @author HeLei
 */
public class LoginEvent extends AbstractAuthenticationEvent {
	private static final long serialVersionUID = 3451596905017124116L;

	public LoginEvent(Authentication authentication) {
		super(authentication);
	}
}
