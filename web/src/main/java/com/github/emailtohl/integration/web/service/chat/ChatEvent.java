package com.github.emailtohl.integration.web.service.chat;

import java.io.Serializable;

import com.github.emailtohl.integration.web.message.event.AuthenticationEvent;
/**
 * 聊天相关的事件
 * @author HeLei
 */
public class ChatEvent extends AuthenticationEvent {
	private static final long serialVersionUID = -1620174996342472535L;
	private ChatMessage message;
	
	public ChatEvent(Serializable source) {
		super(source);
	}

	public ChatMessage getMessage() {
		return message;
	}

	public void setMessage(ChatMessage message) {
		this.message = message;
	}

}
