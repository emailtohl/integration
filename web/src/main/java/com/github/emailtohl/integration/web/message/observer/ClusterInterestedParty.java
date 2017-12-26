package com.github.emailtohl.integration.web.message.observer;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.web.message.event.ClusterEvent;
/**
 * 对集群事件感兴趣的监听器
 * @author HeLei
 */
@Service
public class ClusterInterestedParty implements ApplicationListener<ClusterEvent> {
	private static final Logger log = LogManager.getLogger();

	@Inject ServletContext servletContext;
	@Inject SimpleApplicationEventMulticaster clusterEventMulticaster;

	@Override
	public void onApplicationEvent(ClusterEvent event) {
		log.debug("Cluster event for context {} received in context {}.", event.getSource(),
				servletContext.getContextPath());
	}
}
