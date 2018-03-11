package com.github.emailtohl.integration.web.service.flow;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.Principal;
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

import com.github.emailtohl.integration.common.websocket.Configurator;
import com.github.emailtohl.integration.core.auth.AuthenticationImpl;
import com.github.emailtohl.integration.core.auth.UserDetailsImpl;
import com.github.emailtohl.integration.web.message.event.FlowNotifyEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
/**
 * 通知用户，既是websocket，也是Spring管理的bean，并且关注了UserNotifyEvent事件，可将事件转发给前端用户
 * @author HeLei
 */
@ServerEndpoint(value = "/services/notify", configurator = Configurator.class)
public class UserNotifyWebSocket implements ApplicationListener<FlowNotifyEvent> {
	private static final Logger logger = LogManager.getLogger();

	private String userId;
	private Session session;
	@Inject
	Gson gson;
	Type type = new TypeToken<Map<String, Object>>() {
	}.getType();

	@OnOpen
	public void open(Session session) {
		this.session = session;
		Principal principal = Configurator.getExposedPrincipal(session);
		if (principal instanceof AuthenticationImpl) {
			AuthenticationImpl auth = (AuthenticationImpl) principal;
			if (auth.getPrincipal() instanceof UserDetailsImpl) {
				UserDetailsImpl detail = (UserDetailsImpl) auth.getPrincipal();
				this.userId = detail.getId() == null ? null : detail.getId().toString();
			}
		}
	}
	
	@Override
	public void onApplicationEvent(FlowNotifyEvent event) {
		String json = (String) event.getSource();
		Map<String, Object> map = gson.fromJson(json, type);
		logger.debug(map);
		Object assignee = map.get("assignee");
		if (assignee instanceof String && ((String) assignee).equals(this.userId)) {
			try {
				session.getBasicRemote().sendText(json);
			} catch (IOException e) {
				logger.catching(e);
			}
		}
	}

	@OnMessage
	public void receive(String message, Session session) {
		logger.debug(message);
		Map<String, Object> map = gson.fromJson(message, type);
		Object userId = map.get("userId");
		if (userId instanceof String) {
			this.userId = (String) userId;
		} else if (userId instanceof Number) {
			this.userId = userId.toString();
		}
	}

	@OnClose
	public void close() {
		logger.info("node connection closed.");
	}
}
