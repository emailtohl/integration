package com.github.emailtohl.integration.web.aop;

import static org.springframework.aop.support.AopUtils.isAopProxy;
import static org.springframework.aop.support.AopUtils.isCglibProxy;
import static org.springframework.aop.support.AopUtils.isJdkDynamicProxy;
import static org.junit.Assert.*;

import javax.inject.Inject;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.web.WebTestData;
import com.google.gson.Gson;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class UserServiceProxyTest {
	Gson gson = new Gson();
	@Inject
	EmployeeService employeeService;
	@Inject
	CustomerService customerService;
	@Inject
	UserServiceProxy userServiceProxy;
	@Inject
	IdentityService identityService;
	@Inject
	WebTestData td;

	@Before
	public void setUp() throws Exception {
		System.out.println(isAopProxy(userServiceProxy));
        System.out.println(isCglibProxy(userServiceProxy));
        System.out.println(isJdkDynamicProxy(userServiceProxy));
        
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreate() {
		Employee e = employeeService.create(new Employee());
		assertNotNull(e);
		
		User u = identityService.createUserQuery().userId(e.getId().toString()).singleResult();
		assertNotNull(u);
		System.out.println(u);
		
		Customer c = customerService.create(new Customer());
		assertNotNull(c);
		u = identityService.createUserQuery().userId(c.getId().toString()).singleResult();
		assertNotNull(u);
		System.out.println(u);
	}
	
	@Test
	public void testUpdate() {
		Employee e = new Employee();
		e.setName("update");
		e.getRoles().add(td.role_manager);
		e.getRoles().add(td.role_staff);
		e = employeeService.update(Config.FOO_ID, e);
		assertNotNull(e);
		
		User u = identityService.createUserQuery().userId(e.getId().toString()).singleResult();
		assertNotNull(u);
		System.out.println(u);
		
		Customer c = new Customer();
		c.setName("update");
		c.getRoles().add(td.role_staff);
		c = customerService.update(Config.BAZ_ID, c);
		assertNotNull(c);
		u = identityService.createUserQuery().userId(c.getId().toString()).singleResult();
		assertNotNull(u);
		System.out.println(u);
		
		identityService.createUserQuery().userId(Config.FOO_ID.toString());
	}

}
