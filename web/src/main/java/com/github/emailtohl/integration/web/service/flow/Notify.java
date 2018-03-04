package com.github.emailtohl.integration.web.service.flow;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;

import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;

/**
 * 用户通知服务
 * @author HeLei
 */
public class Notify implements ExecutionListener, TaskListener, Serializable {
	private static final long serialVersionUID = -6779971905269762380L;
	final UserService userService;
	final CustomerService customerService;
	final EmployeeService employeeService;
	
	public Notify(UserService userService, CustomerService customerService, EmployeeService employeeService) {
		super();
		this.userService = userService;
		this.customerService = customerService;
		this.employeeService = employeeService;
	}

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		System.out.println(execution.getClass().getSimpleName() + ", " + execution.getEventName());
		execution.getVariableNames().forEach(varName -> {
			System.out.println(varName + " : " + execution.getVariable(varName));
		});
	}

	@Override
	public void notify(DelegateTask delegateTask) {
		System.out.println(delegateTask.getClass().getSimpleName() + ", " + delegateTask.getEventName());
		System.out.println("task: " + delegateTask.getTaskDefinitionKey() + " " + delegateTask.getName()
				+ "  Assignee: " + delegateTask.getAssignee());
		delegateTask.getVariableNames().forEach(varName -> {
			System.out.println(varName + " : " + delegateTask.getVariable(varName));
		});
	}

}
