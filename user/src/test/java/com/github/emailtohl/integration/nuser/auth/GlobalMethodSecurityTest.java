package com.github.emailtohl.integration.nuser.auth;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.service.CustomerAuditedService;
import com.github.emailtohl.integration.nuser.service.CustomerService;
import com.github.emailtohl.integration.nuser.service.EmployeeAuditedService;
import com.github.emailtohl.integration.nuser.service.EmployeeService;
import com.github.emailtohl.integration.nuser.service.RoleAuditedService;
import com.github.emailtohl.integration.nuser.service.RoleService;
/**
 * 在接口处声明了权限，对这些声明进行测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SecurityConfiguration.class)
public class GlobalMethodSecurityTest {
	@Inject
	SecurityContextManager scm;
	@Inject
	RoleService roleService;
	@Inject
	EmployeeService employeeService;
	@Inject
	CustomerService customerService;
	@Inject
	CustomerAuditedService customerAuditedService;
	@Inject
	EmployeeAuditedService employeeAuditedService;
	@Inject
	RoleAuditedService roleAuditedService;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = AccessDeniedException.class)
	public void testRoleService() {
		scm.clearContext();
		try {
			roleService.getAuthorities();
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		scm.setEmailtohl();
		roleService.getAuthorities();
		scm.setBar();
		roleService.getAuthorities();
	}
	
	@Test
	public void testEmployeeService() {
		Employee bar = null;
		ExecResult r = null;
		scm.clearContext();
		try {
			bar = employeeService.grandRoles(1L, "a", "b", "c");
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(bar);
		try {
			r = employeeService.resetPassword(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(r);
		try {
			bar = employeeService.lock(1L, true);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(bar);
		
		scm.setEmailtohl();
		bar = employeeService.grandRoles(1L, "a", "b", "c");
		assertNotNull(bar);
		r = employeeService.resetPassword(1L);
		assertTrue(r.ok);
		bar = employeeService.lock(1L, false);
		
		scm.setBar();
		try {
			bar = employeeService.grandRoles(1L, "a", "b", "c");
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			r = employeeService.resetPassword(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			bar = employeeService.lock(1L, false);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
	}

	@Test
	public void testCustomerService() {
		Customer baz = null;
		ExecResult r = null;
		scm.clearContext();
		try {
			baz = customerService.grandRoles(1L, "a", "b", "c");
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		try {
			baz = customerService.grandLevel(1L, Customer.Level.VIP);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		try {
			r = customerService.resetPassword(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(r);
		try {
			baz = customerService.lock(1L, true);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		
		scm.setEmailtohl();
		baz = customerService.grandRoles(1L, "a", "b", "c");
		assertNotNull(baz);
		baz = customerService.grandLevel(1L, Customer.Level.VIP);
		assertNotNull(baz);
		r = employeeService.resetPassword(1L);
		assertTrue(r.ok);
		baz = customerService.lock(1L, false);
		
		scm.setBar();
		try {
			baz = customerService.grandRoles(1L, "a", "b", "c");
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			baz = customerService.grandLevel(1L, Customer.Level.VIP);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			r = customerService.resetPassword(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			baz = customerService.lock(1L, false);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
	}
	
}
