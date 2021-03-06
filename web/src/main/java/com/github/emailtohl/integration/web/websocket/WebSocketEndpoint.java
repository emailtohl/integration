package com.github.emailtohl.integration.web.websocket;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.core.auth.AuthenticationImpl;
import com.github.emailtohl.integration.core.auth.UserDetailsImpl;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.cluster.ClusterBoot;
import com.github.emailtohl.integration.web.cluster.ClusterEvent;
import com.github.emailtohl.integration.web.service.chat.Chat;
import com.github.emailtohl.integration.web.service.chat.ChatEvent;
import com.github.emailtohl.integration.web.service.chat.ChatService;
import com.github.emailtohl.integration.web.service.flow.FlowNotifyEvent;
import com.github.emailtohl.integration.web.service.systemInfo.SystemInfoEvent;
import com.google.gson.Gson;
/**
 * Websocket是双向通信，所以它不仅可作为服务端，也可以作为客户端
 * @author HeLei
 */
@ServerEndpoint(value = "/websocket/{securityCode}", configurator = Configurator.class)
public class WebSocketEndpoint {
	// 用户id和websocket session的
	private static final Logger LOG = LogManager.getLogger();
	private volatile String userId;
	private Session session;
	@Inject
	CorePresetData corePresetData;
	@Inject
	ServletContext servletContext;
	@Inject
	Environment env;
	@Inject
	Gson gson;
	@Inject
	ChatService chatService;
	@Inject
	EventListener eventListener;
	
	@OnOpen
	public void onOpen(EndpointConfig config, Session session, @PathParam("securityCode") String securityCode) throws IOException {
		LOG.debug(config.getUserProperties());
		if (!ClusterBoot.SECURITY_CODE.equals(securityCode)) {
			LOG.debug(CloseReason.CloseCodes.VIOLATED_POLICY.name());
			session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Illegal Code"));
			return;
		}
		this.session = session;

		Principal principal = Configurator.getExposedPrincipal(session);
		if (principal instanceof AuthenticationImpl) {
			AuthenticationImpl auth = (AuthenticationImpl) principal;
			if (auth.getPrincipal() instanceof UserDetailsImpl) {
				UserDetailsImpl detail = (UserDetailsImpl) auth.getPrincipal();
				userId = detail.getId() == null ? null : detail.getId().toString();
			}
		}
		if (!StringUtils.hasText(userId)) {
			LOG.warn("userId is empty");
			userId = corePresetData.user_anonymous.getId().toString();
		}
		eventListener.addEndpoint(this);
	}

	@OnMessage
	public void onMessage(String json) {
		Message message = gson.fromJson(json, Message.class);
		if (message.getMessageType() == null) {
			return;
		}
		switch (message.getMessageType()) {
		case refreshUserId:
			if (message.data instanceof String || message.data instanceof Number)
				userId = message.data.toString().split("\\.")[0];
			else
				userId = corePresetData.user_anonymous.getId().toString();
			break;
		case flowNotify:
			break;
		case systemInfo:
			break;
		case chat:// 收到前端聊天消息后，先存储此消息，然后再群发给其他人，存储消息后，会发起消息事件
			if (message.getData() instanceof Map) {
				String data = gson.toJson(message.getData());
				Chat chat = gson.fromJson(data, Chat.class);
				if (StringUtils.hasText(userId)) {
					chatService.save(userId, chat);
				}
			}
			break;
		default:
			break;
		}
	}

	@OnClose
	public void onClose(CloseReason reason) throws IOException {
		Principal principal = Configurator.getExposedPrincipal(session);
		if (principal != null) {
			LOG.info("Node {} disconnected.", principal.getName());
		}
		if (session.isOpen()) {
			session.close();
		}
		eventListener.remove(this);
	}
	
	@OnError
	public void onError(Throwable t) throws IOException {
		LOG.catching(t);
		if (session.isOpen()) {
			session.close();
		}
		eventListener.remove(this);
	}
	
	/**
	 * 响应事件的回调
	 * @param event
	 */
	@Async
	public void onEvent(ClusterEvent event) {
		try {
			// Activiti框架触发事件通知ActivitiListener；
			// 然后ActivitiListener发布事件被EventListener收到；
			// 最后EventListener将事件通知给每个WebSocketEndpoint。
			if (event instanceof FlowNotifyEvent) {
				FlowNotifyEvent e = (FlowNotifyEvent) event;
				Set<String> toUserIds = e.getToUserIds();
				if (toUserIds.contains(userId)) {
					Message msg = new Message();
					msg.id = UUID.randomUUID().toString();
					msg.time = new Date();
					msg.toUserIds.addAll(toUserIds);
					msg.messageType = MessageType.flowNotify;
					msg.data = e;
					session.getBasicRemote().sendText(gson.toJson(msg));
				}
			} else if (event instanceof SystemInfoEvent) {
				SystemInfoEvent e = (SystemInfoEvent) event;
				Message msg = new Message();
				msg.id = UUID.randomUUID().toString();
				msg.time = new Date();
				msg.toUserIds.add(userId);
				msg.messageType = MessageType.systemInfo;
				msg.data = e.getSystemInfo();
				session.getBasicRemote().sendText(gson.toJson(msg));
			} else if (event instanceof ChatEvent) {// 将消息发送给包括自己的所有人
				ChatEvent e = (ChatEvent) event;
				if (e.getChat() != null && StringUtils.hasText(e.getChat().getUserId())) {
					Message msg = new Message();
					msg.id = UUID.randomUUID().toString();
					msg.time = new Date();
					msg.toUserIds.add(userId);
					msg.messageType = MessageType.chat;
					msg.data = e.getChat();
					session.getBasicRemote().sendText(gson.toJson(msg));
				}
			}
		} catch (IOException et) {
			LOG.catching(et);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((session == null) ? 0 : session.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebSocketEndpoint other = (WebSocketEndpoint) obj;
		if (session == null) {
			if (other.session != null)
				return false;
		} else if (!session.equals(other.session))
			return false;
		return true;
	}
}
