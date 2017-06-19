package com.github.emailtohl.integration.message.event;
/**
 * 登录相关的事件
 * @author HeLei
 * @date 2017.02.04
 */
public class LoginEvent extends AuthenticationEvent {
	private static final long serialVersionUID = 3451596905017124116L;

	public LoginEvent(String username) {
		super(username);
	}
}
