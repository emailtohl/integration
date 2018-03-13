package com.github.emailtohl.integration.web.eventlistener;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
/**
 * 对认证事件感兴趣的监听器
 * @author HeLei
 */
@Service
public class AuthenticationInterestedParty implements ApplicationListener<AuthenticationEvent> {
	private static final Logger log = LogManager.getLogger();

	@Inject ServletContext servletContext;

	@Override
	public void onApplicationEvent(AuthenticationEvent event) {
		log.debug("Authentication event from context {} received in context {}.", event.getSource(),
				this.servletContext.getContextPath());
	}
}
