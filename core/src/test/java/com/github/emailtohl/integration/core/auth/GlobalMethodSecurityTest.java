package com.github.emailtohl.integration.core.auth;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleAuditedService;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.service.CustomerAuditedService;
import com.github.emailtohl.integration.core.user.service.CustomerService;
import com.github.emailtohl.integration.core.user.service.EmployeeAuditedService;
import com.github.emailtohl.integration.core.user.service.EmployeeService;
/**
 * 在接口处声明了权限，对这些声明进行测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SecurityConfiguration.class)
public class GlobalMethodSecurityTest {
	CoreTestData td = new CoreTestData();
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
			bar = employeeService.enabled(1L, true);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(bar);
		try {
			r = employeeService.updatePassword(1002, "678901", "token");
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		
		
		scm.setEmailtohl();
		bar = employeeService.grandRoles(1L, "a", "b", "c");
		assertNotNull(bar);
		r = employeeService.resetPassword(1L);
		assertTrue(r.ok);
		bar = employeeService.enabled(1L, false);
		try {
			r = employeeService.updatePassword(1002, "678901", "token");
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
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
			bar = employeeService.enabled(1L, false);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
		System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
		r = employeeService.updatePassword(1002, "678901", "token");
		assertTrue(r.ok);
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
			baz = customerService.enabled(1L, true);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		try {
			r = customerService.updatePassword(td.baz.getEmail(), "678901", "token");
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		
		scm.setEmailtohl();
		baz = customerService.grandRoles(1L, "a", "b", "c");
		assertNotNull(baz);
		baz = customerService.grandLevel(1L, Customer.Level.VIP);
		assertNotNull(baz);
		r = employeeService.resetPassword(1L);
		assertTrue(r.ok);
		baz = customerService.enabled(1L, false);
		try {
			r = customerService.updatePassword(td.baz.getEmail(), "678901", "token");
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
		scm.setBaz();
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
			baz = customerService.enabled(1L, false);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
		r = customerService.updatePassword(td.baz.getCellPhone(), "678901", "token");
		assertTrue(r.ok);
	}
	
	@Test
	public void testCustomerAuditedService() {
		Customer baz = null;
		List<Tuple<Customer>> ls = null;
		scm.clearContext();
		try {
			baz = customerAuditedService.getCustomerAtRevision(1L, 1);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		try {
			ls = customerAuditedService.getCustomerRevision(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(ls);
		
		scm.setBaz();
		try {
			baz = customerAuditedService.getCustomerAtRevision(1L, 1);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		assertNull(baz);
		try {
			ls = customerAuditedService.getCustomerRevision(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
		scm.setEmailtohl();
		baz = customerAuditedService.getCustomerAtRevision(1L, 1);
		assertNotNull(baz);
		ls = customerAuditedService.getCustomerRevision(1L);
		assertNotNull(ls);
	}
	
	@Test
	public void testEmployeeAuditedService() {
		Employee bar = null;
		List<Tuple<Employee>> ls = null;
		scm.clearContext();
		try {
			bar = employeeAuditedService.getEmployeeAtRevision(1L, 1);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(bar);
		try {
			ls = employeeAuditedService.getEmployeeRevision(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(ls);
		
		scm.setBaz();
		try {
			bar = employeeAuditedService.getEmployeeAtRevision(1L, 1);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		assertNull(bar);
		try {
			ls = employeeAuditedService.getEmployeeRevision(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
		scm.setEmailtohl();
		bar = employeeAuditedService.getEmployeeAtRevision(1L, 1);
		assertNotNull(bar);
		ls = employeeAuditedService.getEmployeeRevision(1L);
		assertNotNull(ls);
	}
	
	@Test
	public void testRoleAuditedService() {
		Role role = null;
		List<Tuple<Role>> ls = null;
		scm.clearContext();
		try {
			role = roleAuditedService.getRoleAtRevision(1L, 1);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(role);
		try {
			ls = roleAuditedService.getRoleRevision(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(ls);
		
		scm.setBaz();
		try {
			role = roleAuditedService.getRoleAtRevision(1L, 1);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		assertNull(role);
		try {
			ls = roleAuditedService.getRoleRevision(1L);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
		scm.setEmailtohl();
		role = roleAuditedService.getRoleAtRevision(1L, 1);
		assertNotNull(role);
		ls = roleAuditedService.getRoleRevision(1L);
		assertNotNull(ls);
	}
}
