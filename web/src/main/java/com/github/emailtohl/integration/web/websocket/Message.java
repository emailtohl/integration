package com.github.emailtohl.integration.web.websocket;

import java.io.Serializable;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 消息实体
 * 
 * @author HeLei
 */
public class Message implements Cloneable, Serializable {
	private static final long serialVersionUID = -2412286762156636529L;

	String id;
	Date time;
	MessageType messageType;
	String userId;
	Object data;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", time=" + time + ", messageType=" + messageType + ", userId=" + userId
				+ ", data=" + data + "]";
	}

}
