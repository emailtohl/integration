package com.github.emailtohl.integration.web.websocket;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.github.emailtohl.integration.web.message.event.ClusterEvent;
/**
 * 事件监听器
 * @author HeLei
 */
@Component
public class EventListener implements ApplicationListener<ClusterEvent> {
	private final Set<WebSocketEndpoint> endpoints = new CopyOnWriteArraySet<>();

	@Override
	public void onApplicationEvent(ClusterEvent event) {
		for (WebSocketEndpoint endpoint : endpoints) {
			endpoint.onEvent(event);
		}
//		endpoints.stream().parallel().peek(endpoint -> endpoint.onEvent(event));
	}
	
	public void addEndpoint(WebSocketEndpoint endpoint) {
		endpoints.add(endpoint);
	}

	public void remove(WebSocketEndpoint endpoint) {
		endpoints.remove(endpoint);
	}
}
