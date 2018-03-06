package com.github.emailtohl.integration.web.message.event;

import java.io.Serializable;
/**
 * 用户通知事件
 * @author HeLei
 */
public class UserNotifyEvent extends ClusterEvent {
	private static final long serialVersionUID = -482996726719979688L;

	public UserNotifyEvent(Serializable source) {
		super(source);
	}

}
