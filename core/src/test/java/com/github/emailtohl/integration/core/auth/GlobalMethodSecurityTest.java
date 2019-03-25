package com.github.emailtohl.integration.core.auth;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

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

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleAuditedService;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.role.RoleType;
import com.github.emailtohl.integration.core.user.customer.CustomerAuditedService;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeAuditedService;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.lib.jpa.AuditedRepository.RevTuple;
/**
 * 在接口处声明了权限，对这些声明进行测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SecurityConfiguration.class)
public class GlobalMethodSecurityTest {
	@Inject
	CoreTestData td;
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
			// 未登录时不能访问被权限包含的方法
			roleService.getAuthorities();
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		scm.setEmailtohl();
		// 登录的用户可以调用该方法
		roleService.getAuthorities();
		Role r = new Role("test", RoleType.EMPLOYEE, "test");
		r = roleService.create(r);
		roleService.update(r.getId(), new Role("test", RoleType.CUSTOMER, "for update"));
		// 只要登录，即可查询角色列表
		scm.setBar();
		roleService.getAuthorities();
		// bar没有role权限，故会抛出AccessDeniedException
		roleService.update(r.getId(), new Role("test", RoleType.EMPLOYEE, "for update again"));
	}
	
	@Test
	public void testEmployeeService() {
		Integer empNum = 1002;
		Employee bar = null;
		ExecResult r = null;
		scm.clearContext();
		try {
			employeeService.create(new Employee());
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		try {
			bar = employeeService.get(td.bar.getId());
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(bar);
		try {
			// 传入参数是否正确并不重要，关键是测试被权限包含的方法是否能被调用
			bar = employeeService.grandRoles(td.bar.getId(), "a", "b", "c");
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(bar);
		try {
			r = employeeService.resetPassword(td.bar.getId());
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(r);
		try {
			bar = employeeService.enabled(td.bar.getId(), true);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(bar);
		try {
			r = employeeService.updatePassword(empNum, "678901", "token");
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		
		scm.setEmailtohl();
		employeeService.create(new Employee());
		bar = employeeService.get(td.bar.getId());
		assertNotNull(bar);
		bar = employeeService.grandRoles(td.bar.getId(), "a", "b", "c");
		assertNotNull(bar);
		r = employeeService.resetPassword(td.bar.getId());
		assertTrue(r.ok);
		bar = employeeService.enabled(td.bar.getId(), false);
		bar = employeeService.enabled(td.bar.getId(), true);
		try {
			r = employeeService.updatePassword(empNum, "678901", "token");
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
		scm.setBar();
		try {
			employeeService.create(new Employee());
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		bar = employeeService.get(td.bar.getId());
		assertNotNull(bar);
		try {
			bar = employeeService.grandRoles(td.bar.getId(), "a", "b", "c");
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			r = employeeService.resetPassword(td.bar.getId());
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			bar = employeeService.enabled(td.bar.getId(), false);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
		System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
		r = employeeService.updatePassword(empNum, "678901", "token");
		assertTrue(r.ok);
	}

	@Test
	public void testCustomerService() {
		Customer baz = null;
		ExecResult r = null;
		scm.clearContext();
		
		try {
			baz = customerService.get(td.baz.getId());
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		try {
			baz = customerService.getByUsername(randomCellPhoneOrEmail(td.baz));
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		try {
			baz = customerService.update(td.baz.getId(), new Customer());
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
//		assertNull(baz);
		try {
			baz = customerService.grandRoles(td.baz.getId(), "a", "b", "c");
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		try {
			baz = customerService.grandLevel(td.baz.getId(), Customer.Level.VIP);
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(baz);
		try {
			r = customerService.resetPassword(td.baz.getId());
		} catch (Exception e) {
			assertTrue(e instanceof AuthenticationCredentialsNotFoundException);
		}
		assertNull(r);
		try {
			baz = customerService.enabled(td.baz.getId(), true);
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
		baz = customerService.get(td.baz.getId());
		assertNotNull(baz);
		baz = customerService.getByUsername(randomCellPhoneOrEmail(td.baz));
		assertNotNull(baz);
		td.baz.setName("_baz");
		baz = customerService.update(td.baz.getId(), td.baz);
		td.baz.setName("baz");
		baz = customerService.update(td.baz.getId(), td.baz);
//		assertNotNull(baz);
		baz = customerService.grandRoles(td.baz.getId(), "a", "b", "c");
		assertNotNull(baz);
		baz = customerService.grandLevel(td.baz.getId(), Customer.Level.VIP);
		assertNotNull(baz);
		r = employeeService.resetPassword(td.baz.getId());
		assertTrue(r.ok);
		baz = customerService.enabled(td.baz.getId(), false);
		baz = customerService.enabled(td.baz.getId(), true);
		try {
			r = customerService.updatePassword(td.baz.getEmail(), "678901", "token");
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		
		scm.setBaz();
		baz = customerService.get(td.baz.getId());
		assertNotNull(baz);
		baz = customerService.getByUsername(randomCellPhoneOrEmail(td.baz));
		assertNotNull(baz);
		baz = customerService.update(td.baz.getId(), new Customer());
//		assertNotNull(baz);
		try {
			baz = customerService.grandRoles(td.baz.getId(), "a", "b", "c");
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			baz = customerService.grandLevel(td.baz.getId(), Customer.Level.VIP);
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			r = customerService.resetPassword(td.baz.getId());
		} catch (Exception e) {
			assertTrue(e instanceof AccessDeniedException);
		}
		try {
			baz = customerService.enabled(td.baz.getId(), false);
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
		List<RevTuple<Customer>> ls = null;
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
		List<RevTuple<Employee>> ls = null;
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
		List<RevTuple<Role>> ls = null;
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
	
	
	Random r = new Random();
	String randomCellPhoneOrEmail(Customer c) {
		if (r.nextBoolean()) {
			return c.getCellPhone();
		} else {
			return c.getEmail();
		}
	}
}
