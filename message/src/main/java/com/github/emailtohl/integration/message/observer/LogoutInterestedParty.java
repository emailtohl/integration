package com.github.emailtohl.integration.message.observer;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.message.event.LogoutEvent;
/**
 * 对登出感兴趣的监听器
 * @author HeLei
 * @date 2017.02.04
 */
@Service
public class LogoutInterestedParty implements ApplicationListener<LogoutEvent> {
	private static final Logger log = LogManager.getLogger();

	@Inject ServletContext servletContext;

	@Override
	@Async
	public void onApplicationEvent(LogoutEvent event) {
		log.info("Logout event for user {} received in context {}.", event.getSource(),
				this.servletContext.getContextPath());

		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			log.error(e);
		}
	}
}
