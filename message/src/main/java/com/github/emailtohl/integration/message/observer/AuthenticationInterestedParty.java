package com.github.emailtohl.integration.message.observer;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.message.event.AuthenticationEvent;
/**
 * 对认证事件感兴趣的监听器
 * @author HeLei
 * @date 2017.02.04
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
