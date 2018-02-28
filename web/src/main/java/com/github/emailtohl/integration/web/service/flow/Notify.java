package com.github.emailtohl.integration.web.service.flow;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;

/**
 * 用户通知服务
 * @author HeLei
 */
public class Notify implements ExecutionListener, Serializable {
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
		System.out.println("VariableNames: " + execution.getVariableNames());
	}

}
