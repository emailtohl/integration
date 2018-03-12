package com.github.emailtohl.integration.web.service.chat;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
/**
 * 被websocket序列化的类
 * @author HeLei
 */
public class Chat implements Cloneable, Serializable {
	private static final long serialVersionUID = 7381096058154145043L;
	private String content;
	private String userId;
	private Date time;
	private String name;
	private String nickname;// 可存储第三方昵称
	private String email;
	private String iconSrc;
	private String cellPhone;
	
	public Chat() {}
	public Chat(Map<?, ?> map) {
		
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getIconSrc() {
		return iconSrc;
	}
	public void setIconSrc(String iconSrc) {
		this.iconSrc = iconSrc;
	}
	
	public String getCellPhone() {
		return cellPhone;
	}
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}
	
	@Override
	public String toString() {
		return "Chat [content=" + content + ", userId=" + userId + ", time=" + time + ", name=" + name + ", nickname="
				+ nickname + ", email=" + email + ", iconSrc=" + iconSrc + ", cellPhone=" + cellPhone + "]";
	}

}
