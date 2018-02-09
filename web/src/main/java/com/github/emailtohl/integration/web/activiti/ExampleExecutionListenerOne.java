package com.github.emailtohl.integration.web.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * 监听异常
 * @author HeLei
 */
public class ExampleExecutionListenerOne implements ExecutionListener {
	private static final long serialVersionUID = 2271409899236671183L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		execution.setVariable("variableSetInExecutionListener", "firstValue");
		execution.setVariable("eventReceived", execution.getEventName());
		
	}
}
