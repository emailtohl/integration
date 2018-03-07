package com.github.emailtohl.integration.web.message.observer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import com.github.emailtohl.integration.web.message.event.UserNotifyEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 通知用户 TODO
 * @author HeLei
 */
@ServerEndpoint(value = "/services/notify", configurator = SpringConfigurator.class)
public class UserNotifyInterestedParty implements ApplicationListener<UserNotifyEvent> {
	private static final Logger logger = LogManager.getLogger();

	private String userId;
	private Session session;
	@Inject
	Gson gson;
	Type type = new TypeToken<Map<String, Object>>() {
	}.getType();

	@Override
	public void onApplicationEvent(UserNotifyEvent event) {
		String json = (String) event.getSource();
		Map<String, Object> map = gson.fromJson(json, type);
		logger.debug(map);
		Object assignee = map.get("assignee");
		if (assignee instanceof String) {
			Long userId = Long.valueOf((String) assignee);
			if (userId.equals(this.userId)) {
				try {
					session.getBasicRemote().sendText(json);
				} catch (IOException e) {
					logger.catching(e);
				}
			}
		}
	}

	@OnOpen
	public void open(Session session) {
		this.session = session;
	}

	@OnMessage
	public void receive(String message, Session session) {
		logger.debug(message);
		Map<String, Object> map = gson.fromJson(message, type);
		Object userId = map.get("userId");
		if (userId instanceof String) {
			this.userId = (String) userId;
		}
	}

	@OnClose
	public void close() {
		logger.info("node connection closed.");
	}
}
