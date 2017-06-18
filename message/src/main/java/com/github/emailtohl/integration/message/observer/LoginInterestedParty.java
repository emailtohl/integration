package com.github.emailtohl.integration.message.observer;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.message.event.LoginEvent;
/**
 * 对登录事件感兴趣的监听器
 * @author HeLei
 * @date 2017.02.04
 */
@Service
public class LoginInterestedParty implements ApplicationListener<LoginEvent> {
	private static final Logger log = LogManager.getLogger();

	@Inject ServletContext servletContext;

	@Override
	public void onApplicationEvent(LoginEvent event) {
		log.info("Login event for user {} received in context {}.", event.getSource(),
				this.servletContext.getContextPath());
	}
}
