package com.github.emailtohl.integration.web.message.event;

import java.io.Serializable;
/**
 * 认证相关的事件
 * @author HeLei
 */
public abstract class AuthenticationEvent extends ClusterEvent {
	private static final long serialVersionUID = -759325995400716933L;

	public AuthenticationEvent(Serializable source) {
		super(source);
	}
}
