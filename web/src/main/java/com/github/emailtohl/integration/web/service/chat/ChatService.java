package com.github.emailtohl.integration.web.service.chat;
/**
 * 聊天程序的接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface ChatService {
	/**
	 * 存储聊天信息
	 * @param username
	 * @param msg
	 */
	void save(String username, ChatMessage msg);
}
