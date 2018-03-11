package com.github.emailtohl.integration.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.emailtohl.integration.common.websocket.Configurator;
import com.github.emailtohl.integration.core.auth.AuthenticationImpl;
import com.github.emailtohl.integration.core.auth.UserDetailsImpl;
import com.github.emailtohl.integration.web.message.event.FlowNotifyEvent;
import com.github.emailtohl.integration.web.message.event.SystemInfoEvent;
import com.github.emailtohl.integration.web.service.chat.ChatEvent;
import com.github.emailtohl.integration.web.service.chat.ChatService;
/**
 * Websocket是双向通信，所以它不仅可作为服务端，也可以作为客户端
 * @author HeLei
 */
@ServerEndpoint(value = "/websocket/{securityCode}", encoders = { Codec.class }, decoders = {
		Codec.class }, configurator = Configurator.class)
@ClientEndpoint(encoders = { Codec.class }, decoders = { Codec.class })
public class WebSocketEndpoint implements ApplicationListener<ApplicationEvent> {
	private static final String SECURITY_CODE = "abc123";
	// 用户id和websocket session的
	private static final Set<Session> SESSIONS = new CopyOnWriteArraySet<>();
	private static final Logger LOG = LogManager.getLogger();
	private Session session;
	@Inject
	private ChatService chatService;
	
	@OnOpen
	public void onOpen(Session session, @PathParam("securityCode") String securityCode) throws IOException {
		if (!SECURITY_CODE.equals(securityCode)) {
			LOG.debug(CloseReason.CloseCodes.VIOLATED_POLICY.name());
			session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Illegal Code"));
			return;
		}
		this.session = session;
		SESSIONS.add(session);
	}

	@OnMessage
	public void onMessage(Message message) {
		switch (message.getMessageType()) {
		case chat:
			break;
		case systemInfo:
			break;
		case flowNotify:
			break;
		default:
			break;
		}
	}

	@OnClose
	public void onClose(CloseReason reason) {
		Principal principal = Configurator.getExposedPrincipal(session);
		if (principal != null) {
			LOG.info("Node {} disconnected.", principal.getName());
		}
		SESSIONS.remove(session);
	}
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		Message msg = new Message();
		msg.id = UUID.randomUUID().toString();
		msg.time = new Date();
		msg.userId = getUserId();
		try {
			if (event instanceof FlowNotifyEvent) {
				FlowNotifyEvent e = (FlowNotifyEvent) event;
				Object assignee = e.getVariables().get("assignee");
				if (assignee instanceof String && ((String) assignee).equals(msg.userId)) {
					msg.messageType = MessageType.flowNotify;
					msg.content = e.getVariables();
					session.getBasicRemote().sendObject(msg);
				}
			} else if (event instanceof SystemInfoEvent) {
				SystemInfoEvent e = (SystemInfoEvent) event;
				msg.messageType = MessageType.systemInfo;
				msg.content = e.getSystemInfo();
				session.getBasicRemote().sendObject(msg);
			} else if (event instanceof ChatEvent) {
				ChatEvent e = (ChatEvent) event;
				msg.messageType = MessageType.flowNotify;
				msg.content = e.getMessage();
				chatService.save(msg.userId, e.getMessage());
				for (Session ss : SESSIONS) {
					if (ss != session) {
						ss.getBasicRemote().sendObject(msg);
					}
				}
			}
		} catch (IOException | EncodeException et) {
			LOG.catching(et);
		}
	}
	
	/**
	 * 获取用户id
	 * @param session
	 * @return
	 */
	private String getUserId() {
		String userId = null;
		Principal principal = Configurator.getExposedPrincipal(session);
		if (principal instanceof AuthenticationImpl) {
			AuthenticationImpl auth = (AuthenticationImpl) principal;
			if (auth.getPrincipal() instanceof UserDetailsImpl) {
				UserDetailsImpl detail = (UserDetailsImpl) auth.getPrincipal();
				userId = detail.getId() == null ? null : detail.getId().toString();
			}
		}
		return userId;
	}
	
}

/**
 * 消息类型
 * 
 * @author HeLei
 */
enum MessageType {
	chat/* 聊天 */, systemInfo/* 系统信息 */, flowNotify/* 流程通知 */
}

/**
 * 消息实体
 * 
 * @author HeLei
 */
class Message implements Cloneable, Serializable {
	private static final long serialVersionUID = -2412286762156636529L;

	String id;
	Date time;
	MessageType messageType;
	String userId;
	Object content;

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

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", time=" + time + ", messageType=" + messageType + ", userId=" + userId
				+ ", content=" + content + "]";
	}

}

/**
 * 序列化编码
 * 
 * @author HeLei
 */
class Codec implements Encoder.BinaryStream<Message>, Decoder.BinaryStream<Message> {
	@Override
	public Message decode(InputStream stream) throws DecodeException, IOException {
		try (ObjectInputStream input = new ObjectInputStream(stream)) {
			return (Message) input.readObject();
		} catch (ClassNotFoundException e) {
			throw new DecodeException((String) null, "Failed to decode.", e);
		}
	}

	@Override
	public void encode(Message event, OutputStream stream) throws IOException {
		try (ObjectOutputStream output = new ObjectOutputStream(stream)) {
			output.writeObject(event);
		}
	}

	@Override
	public void init(EndpointConfig endpointConfig) {
	}

	@Override
	public void destroy() {
	}
}