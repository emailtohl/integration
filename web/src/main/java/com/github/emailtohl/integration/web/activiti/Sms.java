package com.github.emailtohl.integration.web.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 短信通知
 * @author HeLei
 */
public class Sms implements JavaDelegate {
	private static final Logger logger = LogManager.getLogger();
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		logger.info("已发送  " + execution.getVariable("token") + "  给  " + execution.getVariable("cellPhoneNum"));
	}
}
