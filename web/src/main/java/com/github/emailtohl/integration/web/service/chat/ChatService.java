package com.github.emailtohl.integration.web.service.chat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
/**
 * 聊天程序接口的实现类，受spring管理
 * @author HeLei
 */
@Service
public class ChatService {
	private static final Logger log = LogManager.getLogger();
	private static final Map<String, Chat> map = new ConcurrentHashMap<String, Chat>();
	@Inject
	private ApplicationEventPublisher publisher;
	
	public void save(String userId, Chat chat) {
		chat.setUserId(userId);
		chat.setTime(new Date());
		map.put(userId, chat);
		LocalDateTime ldt = LocalDateTime.ofInstant(chat.getTime().toInstant(), ZoneId.systemDefault());
		log.info("At time: {} user: {} say: {}", ldt.toString(), userId, chat.getContent());
		
		publisher.publishEvent(new ChatEvent(getClass().getName(), chat));
	}

}
