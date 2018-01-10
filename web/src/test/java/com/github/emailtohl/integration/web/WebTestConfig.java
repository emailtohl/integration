package com.github.emailtohl.integration.web;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.web.config.DataConfiguration;

/**
 * 对Web层测试的配置
 * @author HeLei
 */
@Configurable
@Import(DataConfiguration.class)
@ComponentScan({"com.github.emailtohl.integration.web.aop", "com.github.emailtohl.integration.web.service"})
@EnableAspectJAutoProxy
public class WebTestConfig {
	@Inject
	UserService userService;
	@Inject
	CustomerService customerService;
	@Inject
	EmployeeService employeeService;
	
	@Bean
	public WebTestData webTestData() {
		WebTestData td = new WebTestData();
		Customer c = customerService.create(td.baz);
		td.baz.setId(c.getId());
		c = customerService.grandRoles(c.getId(), td.baz.roleNames().toArray(new String[td.baz.roleNames().size()]));
		td.baz.getRoles().clear();
		td.baz.getRoles().addAll(c.getRoles());
		
		c = customerService.create(td.qux);
		td.qux.setId(c.getId());
		c = customerService.grandRoles(c.getId(), td.qux.roleNames().toArray(new String[td.qux.roleNames().size()]));
		td.qux.getRoles().clear();
		td.qux.getRoles().addAll(c.getRoles());
		
		c = customerService.getByUsername(Constant.ANONYMOUS_EMAIL);
		td.user_anonymous.setId(c.getId());
		td.user_anonymous.getRoles().clear();
		td.user_anonymous.getRoles().addAll(c.getRoles());
		
		Employee e = employeeService.create(td.foo);
		td.foo.setId(e.getId());
		e = employeeService.grandRoles(e.getId(), td.foo.roleNames().toArray(new String[td.foo.roleNames().size()]));
		td.foo.getRoles().clear();
		td.foo.getRoles().addAll(e.getRoles());
		
		e = employeeService.create(td.bar);
		td.bar.setId(e.getId());
		e = employeeService.grandRoles(e.getId(), td.bar.roleNames().toArray(new String[td.bar.roleNames().size()]));
		td.bar.getRoles().clear();
		td.bar.getRoles().addAll(e.getRoles());
		
		e = employeeService.getByEmpNum(Employee.NO1);
		td.user_admin.setId(e.getId());
		td.user_admin.getRoles().clear();
		td.user_admin.getRoles().addAll(e.getRoles());
		
		e = employeeService.getByEmpNum(Employee.NO_BOT);
		td.user_bot.setId(e.getId());
		td.user_bot.getRoles().clear();
		td.user_bot.getRoles().addAll(e.getRoles());
		
		
		return td;
	}
	
}
