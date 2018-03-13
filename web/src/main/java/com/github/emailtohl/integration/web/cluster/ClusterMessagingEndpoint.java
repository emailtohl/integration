package com.github.emailtohl.integration.web.cluster;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.socket.server.standard.SpringConfigurator;
/**
 * 集群间websocket通信端，由于websocket是双向通信，所以它既是服务端也是客户端。
 * 首先，ClusterBoot调用ClusterEventMulticaster的registerNode打开了集群另一个端点的ClusterMessagingEndpoint；
 * 然后，在@OnOpen方法中反过来向ClusterEventMulticaster.registerEndpoint方法注册自己；
 * 最后，ClusterEventMulticaster在接收到集群事件时，通过send方法对外发送集群事件；
 * 另外，receive方法在收到另一个端点的集群事件时，将该事件交由ClusterEventMulticaster.handleReceivedClusteredEvent统一处理。
 * @author HeLei
 */
@ServerEndpoint(value = "/cluster/{securityCode}", encoders = { Codec.class }, decoders = {
		Codec.class }, configurator = SpringConfigurator.class)
@ClientEndpoint(encoders = { Codec.class }, decoders = { Codec.class })
public class ClusterMessagingEndpoint {
	private static final Logger logger = LogManager.getLogger();

	private Session session;

	@Inject
	ClusterEventMulticaster multicaster;

	@OnOpen
	public void open(Session session) {
		Map<String, String> parameters = session.getPathParameters();
		if (!parameters.containsKey("securityCode") || !ClusterBoot.SECURITY_CODE.equals(parameters.get("securityCode"))) {
			try {
				logger.error("Received connection with illegal code {}.", parameters.get("securityCode"));
				session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Illegal Code"));
			} catch (IOException e) {
				logger.warn("Failed to close illegal connection.", e);
			}
		} else {
			logger.info("Successful connection onOpen.");
			this.session = session;
			multicaster.registerEndpoint(this);
		}
	}

	@OnMessage
	public void receive(ClusterEvent message) {
		multicaster.handleReceivedClusteredEvent(message);
	}

	public void send(ClusterEvent message) {
		try {
			session.getBasicRemote().sendObject(message);
		} catch (IOException | EncodeException e) {
			logger.error("Failed to send message to adjacent node.", e);
		}
	}

	@OnClose
	public void close() {
		logger.info("Cluster node connection closed.");
		multicaster.deregisterEndpoint(this);
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IOException e) {
				logger.warn("Error while closing cluster node connection.", e);
			}
		}
	}

}
