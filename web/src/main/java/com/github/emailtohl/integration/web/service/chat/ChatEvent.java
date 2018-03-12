package com.github.emailtohl.integration.web.service.chat;

import java.io.Serializable;

import com.github.emailtohl.integration.web.message.event.ClusterEvent;
/**
 * 聊天相关的事件
 * @author HeLei
 */
public class ChatEvent extends ClusterEvent {
	private static final long serialVersionUID = -1620174996342472535L;
	private Chat chat;
	
	public ChatEvent(Serializable source) {
		super(source);
	}

	public ChatEvent(Serializable source, Chat chat) {
		super(source);
		this.chat = chat;
	}

	public Chat getChat() {
		return chat;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

}
