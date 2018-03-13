package com.github.emailtohl.integration.web.eventlistener;

import java.io.Serializable;

import com.github.emailtohl.integration.web.cluster.ClusterEvent;
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
