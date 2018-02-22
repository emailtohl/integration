package com.github.emailtohl.integration.web.aop;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.*;
import static org.springframework.aop.support.AopUtils.isAopProxy;
import static org.springframework.aop.support.AopUtils.isCglibProxy;
import static org.springframework.aop.support.AopUtils.isJdkDynamicProxy;

import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.web.config.WebTestConfig;
import com.github.emailtohl.integration.web.config.WebTestData;

/**
 * 对切面的测试
 * 
 * @author HeLei
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestConfig.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class UserServiceProxyTest {
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
	String employeeId, customerId;

	@Before
	public void setUp() throws Exception {
		System.out.println(isAopProxy("isAopProxy: " + userServiceProxy));
        System.out.println(isCglibProxy("isCglibProxy: " + userServiceProxy));
        System.out.println(isJdkDynamicProxy("isJdkDynamicProxy: " + userServiceProxy));

		Employee e = new Employee();
		e.setEmail("hello@world.com");
		e.setName("hello");
		e.setNickname("world");
		e.setPassword("123456");
		e = employeeService.create(e);
		assertNotNull(e);
		
		employeeId = e.getId().toString();
		
		User u = identityService.createUserQuery().userId(employeeId).singleResult();
		assertNotNull(u);
		
		Customer c = new Customer();
		c.setEmail("hello@world.com");
		c.setName("hello");
		c.setNickname("world");
		c.setPassword("123456");
		c = customerService.create(c);
		assertNotNull(c);
		
		customerId = c.getId().toString();
		
		u = identityService.createUserQuery().userId(customerId).singleResult();
		assertNotNull(u);
	}

	@After
	public void tearDown() throws Exception {
		employeeService.delete(Long.valueOf(employeeId));
		User u = identityService.createUserQuery().userId(employeeId).singleResult();
		assertNull(u);
		
		customerService.delete(Long.valueOf(customerId));
		u = identityService.createUserQuery().userId(customerId).singleResult();
		assertNull(u);
	}

	@Test
	public void testUpdate() {
		Employee e = new Employee();
		e.setName("update");
		e.getRoles().add(td.pd.role_manager);
		e.getRoles().add(td.pd.role_staff);
		e = employeeService.update(Long.valueOf(employeeId), e);
		assertNotNull(e);
		
		User u = identityService.createUserQuery().userId(employeeId).singleResult();
		assertEquals("update", u.getFirstName());
		
		Customer c = new Customer();
		c.setName("update");
		c.getRoles().add(td.pd.role_staff);
		c = customerService.update(Long.valueOf(customerId), c);
		assertNotNull(c);
		u = identityService.createUserQuery().userId(customerId).singleResult();
		assertEquals("update", u.getFirstName());
		
	}
	
	@Test
	public void testGrandRoles() {
		employeeService.grandRoles(Long.valueOf(employeeId), td.pd.role_manager.getName(), td.pd.role_staff.getName());
		List<Group> ls = identityService.createGroupQuery().groupMember(employeeId).list();
		assertEquals(2, ls.size());
		ls.forEach(g -> System.out.println(g.getName()));
		
		employeeService.grandRoles(Long.valueOf(employeeId), td.pd.role_guest.getName(), td.pd.role_staff.getName());
		ls = identityService.createGroupQuery().groupMember(employeeId).list();
		assertEquals(2, ls.size());
		ls.forEach(g -> System.out.println(g.getName()));
	}
	
	@Test
	public void testUpdatePassword() {
		Employee e = employeeService.get(Long.valueOf(employeeId));
		
		employeeService.updatePassword(e.getEmpNum(), "oldPassword", "newPassword");
		
		employeeService.resetPassword(e.getId());
		
		Customer c = customerService.get(Long.valueOf(customerId));
		
		customerService.updatePassword(c.getCellPhone(), "newPassword", "token");
		
		customerService.resetPassword(c.getId());
	}

}
