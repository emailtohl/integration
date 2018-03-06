package com.github.emailtohl.integration.web.service.flow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;

import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.web.message.event.UserNotifyEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 用户通知服务
 * 
 * @author HeLei
 */
public class Notify implements ExecutionListener, TaskListener, Serializable {
	private static final long serialVersionUID = -6779971905269762380L;
	private static final Logger logger = LogManager.getLogger();
	final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	final UserService userService;
	final CustomerService customerService;
	final EmployeeService employeeService;
	final ApplicationEventPublisher publisher;

	public Notify(UserService userService, CustomerService customerService, EmployeeService employeeService,
			ApplicationEventPublisher publisher) {
		super();
		this.userService = userService;
		this.customerService = customerService;
		this.employeeService = employeeService;
		this.publisher = publisher;
	}

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		logger.debug(execution.getClass().getSimpleName() + ", " + execution.getEventName());
		execution.getVariableNames().forEach(varName -> {
			logger.debug(varName + " : " + execution.getVariable(varName));
		});
		Map<String, String> args = new HashMap<>();
		args.put("eventName", execution.getEventName());
		args.put("id", execution.getId());
		args.put("processInstanceId", execution.getProcessInstanceId());
		args.put("processBusinessKey", execution.getProcessBusinessKey());
		args.put("currentActivityId", execution.getCurrentActivityId());
		UserNotifyEvent event = new UserNotifyEvent(gson.toJson(args));
		publisher.publishEvent(event);
	}

	@Override
	public void notify(DelegateTask delegateTask) {
		logger.debug(delegateTask.getClass().getSimpleName() + ", " + delegateTask.getEventName());
		logger.debug("task: " + delegateTask.getTaskDefinitionKey() + " " + delegateTask.getName() + "  Assignee: "
				+ delegateTask.getAssignee());
		delegateTask.getVariableNames().forEach(varName -> {
			logger.debug(varName + " : " + delegateTask.getVariable(varName));
		});

		Map<String, Object> args = new HashMap<>();
		args.put("eventName", delegateTask.getEventName());
		args.put("id", delegateTask.getId());
		args.put("processInstanceId", delegateTask.getProcessInstanceId());
		args.put("executionId", delegateTask.getExecutionId());
		args.put("assignee", delegateTask.getAssignee());
		args.put("name", delegateTask.getName());
		args.put("candidates", delegateTask.getCandidates().stream().map(il -> il.getUserId()).collect(Collectors.toList()));
		args.put("dueDate", delegateTask.getDueDate());
		UserNotifyEvent event = new UserNotifyEvent(gson.toJson(args));
		publisher.publishEvent(event);
	}

}
