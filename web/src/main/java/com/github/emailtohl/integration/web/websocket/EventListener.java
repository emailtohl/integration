package com.github.emailtohl.integration.web.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.websocket.CloseReason;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.github.emailtohl.integration.web.cluster.ClusterEvent;
/**
 * 事件监听器
 * @author HeLei
 */
@Component
public class EventListener implements ApplicationListener<ClusterEvent> {
	private final Set<WebSocketEndpoint> endpoints = new CopyOnWriteArraySet<>();
	@Inject
	ApplicationContext context;
	
	@Override
	public void onApplicationEvent(ClusterEvent event) {
		if (event.isRebroadcasted()) {
			return;
		}
		synchronized (endpoints) {
			for (WebSocketEndpoint endpoint : endpoints) {
				endpoint.onEvent(event);
			}
		}
	}
	
	public void addEndpoint(WebSocketEndpoint endpoint) {
		endpoints.add(endpoint);
	}

	public void remove(WebSocketEndpoint endpoint) {
		endpoints.remove(endpoint);
	}
	
	@PreDestroy
	public void close() throws IOException {
		synchronized (endpoints) {
			for (WebSocketEndpoint endpoint : endpoints) {
				endpoint.onClose(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "container close"));
			}
		}
	}
}
