package com.github.emailtohl.integration.web.service.systemInfo;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.emailtohl.integration.web.message.event.SystemInfoEvent;
/**
 * 获取系统信息的定时任务
 * @author HeLei
 */
@Component
public class SystemInfo {
	private static final Logger logger = LogManager.getLogger();
	@Inject
	ApplicationEventPublisher publisher;
	
	@Scheduled(fixedDelay = 5000)
	public void showSystemInfo() {
		Runtime();
	}
	
	public void Runtime() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		Map<String, Serializable> info = new HashMap<>();
		for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
			method.setAccessible(true);
			if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
				Object value;
				try {
					value = method.invoke(operatingSystemMXBean);
					logger.log(Level.TRACE, method.getName() + " = " + value);
					if (value instanceof Serializable) {
						info.put(method.getName(), (Serializable) value);
					}
				} catch (Exception e) {
					logger.info(e);
				} // try
			} // if
		} // for
		publisher.publishEvent(new SystemInfoEvent(getClass().getName(), info));
	}
	
}
