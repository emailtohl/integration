package com.github.emailtohl.integration.web.cluster;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
/**
 * 管理着所有websocket端点：Endpoint
 * 首先，registerNode方法被ClusterBoot调用，创建一个连接到其他端点的ClusterMessagingEndpoint；
 * 然后，registerEndpoint方法是在ClusterMessagingEndpoint打开连接时：@OnOpen，被调用；
 * 最后，通过继承Spring的事件广播器，将集群事件通知给各个ClusterMessagingEndpoint。
 * @author HeLei
 */
// applicationEventMulticaster这个名字是有意义的，spring会识别它并将其用作消息广播的Bean
@Component("applicationEventMulticaster")
public class ClusterEventMulticaster extends SimpleApplicationEventMulticaster {
	private static final Logger logger = LogManager.getLogger();

	private final Set<ClusterMessagingEndpoint> endpoints = new HashSet<>();

	@Inject
	ApplicationContext context;

	@Override
	public final void multicastEvent(ApplicationEvent event) {
		try {
			super.multicastEvent(event);
		} finally {
			try {
				if (event instanceof ClusterEvent && !((ClusterEvent) event).isRebroadcasted())
					publishClusteredEvent((ClusterEvent) event);
			} catch (Exception e) {
				logger.error("Failed to broadcast distributable event to cluster.", e);
			}
		}
	}
	
	@Override
	public final void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
		try {
			super.multicastEvent(event, eventType);
		} finally {
			try {
				if (event instanceof ClusterEvent && !((ClusterEvent) event).isRebroadcasted())
					publishClusteredEvent((ClusterEvent) event);
			} catch (Exception e) {
				logger.error("Failed to broadcast distributable event to cluster.", e);
			}
		}
	}

	protected void publishClusteredEvent(ClusterEvent event) {
		synchronized (endpoints) {
			for (ClusterMessagingEndpoint endpoint : endpoints)
				endpoint.send(event);
		}
	}

	protected void registerEndpoint(ClusterMessagingEndpoint endpoint) {
		synchronized (endpoints) {
			endpoints.add(endpoint);
		}
	}

	protected void deregisterEndpoint(ClusterMessagingEndpoint endpoint) {
		synchronized (endpoints) {
			endpoints.remove(endpoint);
		}
	}

	protected void registerNode(String endpointUrl) {
		logger.info("Connecting to cluster node {}.", endpointUrl);
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			ClusterMessagingEndpoint bean = context.getAutowireCapableBeanFactory()
					.createBean(ClusterMessagingEndpoint.class);
			container.connectToServer(bean, new URI(endpointUrl));
			logger.info("Connected to cluster node {}.", endpointUrl);
		} catch (DeploymentException | IOException | URISyntaxException e) {
			logger.error("Failed to connect to cluster node {}.", endpointUrl, e);
		}
	}

	protected final void handleReceivedClusteredEvent(ClusterEvent event) {
		event.setRebroadcasted();
		multicastEvent(event);
	}

	@PreDestroy
	public void shutdown() {
		synchronized (endpoints) {
			for (ClusterMessagingEndpoint endpoint : endpoints)
				endpoint.close();
		}
	}
}
