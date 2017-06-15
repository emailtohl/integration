package com.github.emailtohl.integration.user.service;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.user.UserTestData;
import com.github.emailtohl.integration.user.entities.Customer;
import com.github.emailtohl.integration.user.entities.Employee;
import com.github.emailtohl.integration.user.entities.User;
import com.github.emailtohl.integration.user.userTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.user.userTestConfig.SecurityConfiguration;

/**
 * 业务类测试
 * @author HeLei
 * @date 2017.06.15
 */
@Transactional
@Rollback(false)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SecurityConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.POSTGRESQL_DB)
public class UserServiceSecurityTest {
	@Inject
	SecurityContextManager securityContextManager;
	@Inject
	UserService userService;
	UserTestData td;
	Employee employee;
	Customer customer;
	long employeeId;
	long customerId;
	final String testPassword = "123456";
	final Pageable pageable = new PageRequest(0, 20);
	
	@Before
	public void setUp() throws ResourceNotFoundException {
		securityContextManager.setEmailtohl();
		td = new UserTestData();
		employee = (Employee) userService.getUserByEmail(td.foo.getEmail());
		employeeId = employee.getId();
		customer = (Customer) userService.getUserByEmail(td.baz.getEmail());
		customerId = customer.getId();
	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testAddEmployee1() {
		securityContextManager.clearContext();
		userService.addEmployee(employee);
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testAddEmployee2() {
		securityContextManager.setBaz();
		userService.addEmployee(employee);
	}
	
	@Test
	public void testAddEmployee3() {
		securityContextManager.setFoo();
		try {
			userService.addEmployee(employee);
		} catch (DataIntegrityViolationException e) {}
	}
	
	@Test
	public void testaddCustomer() {
		userService.addCustomer(customer);
	}
	
	@Test
	public void testEnableUser() {
		userService.enableUser(customerId);
	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testDisableUser1() {
		securityContextManager.clearContext();
		userService.deleteUser(customerId);
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testDisableUser2() {
		securityContextManager.setBar();
		userService.deleteUser(customerId);
	}

	@Test
	public void testDisableUser3() {
		securityContextManager.setEmailtohl();
		userService.deleteUser(customerId);
	}
	
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testGrantRoles1() {
		securityContextManager.clearContext();
		userService.grantRoles(customerId);
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testGrantRoles2() {
		securityContextManager.setBaz();
		userService.grantRoles(customerId);
	}

	@Test
	public void testGrantRoles3() {
		securityContextManager.setEmailtohl();
		userService.grantRoles(customerId);
	}
	
	@Test
	public void testGrantUserRole() {
		securityContextManager.clearContext();
		userService.grantUserRole(customerId);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testChangePassword1() {
		securityContextManager.clearContext();
		userService.changePassword(customer.getEmail(), testPassword);
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testChangePassword2() {
		securityContextManager.setEmailtohl();
		userService.changePassword(customer.getEmail(), testPassword);
	}
	
	@Test
	public void testChangePassword3() {
		securityContextManager.setBaz();
		userService.changePassword(customer.getEmail(), testPassword);
	}
	
	@Test
	public void testChangePasswordByEmail() {
		securityContextManager.clearContext();
		userService.changePasswordByEmail(customer.getEmail(), testPassword);
	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testDeleteUser1() {
		SecurityContextHolder.clearContext();
		userService.deleteUser(customerId);
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testDeleteUser2() {
		securityContextManager.setFoo();
		userService.deleteUser(customerId);
	}
	
	@Test
	public void testDeleteUser3() {
		securityContextManager.setEmailtohl();
		userService.deleteUser(customerId);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testGetUser1() {
		SecurityContextHolder.clearContext();
		userService.getUser(customerId);
	}
	
	@Test
	public void testGetUser2() {
		securityContextManager.setBaz();
		userService.getUser(customerId);
	}
	
	@Test
	public void testGetUser3() {
		securityContextManager.setBar();
		userService.getUser(customerId);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testGetUserByEmail1() throws ResourceNotFoundException {
		SecurityContextHolder.clearContext();
		try {
			userService.getUserByEmail(customer.getEmail());
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testGetUserByEmail2() throws ResourceNotFoundException {
		securityContextManager.setBar();
		User u = userService.getUserByEmail(employee.getEmail());
		System.out.println(u);
		securityContextManager.setBaz();
		u = userService.getUserByEmail(customer.getEmail());
		System.out.println(u);
		u = userService.getUserByEmail(employee.getEmail());
		System.out.println(u);
	}

	@Test
	public void testGetUserByEmail3() throws ResourceNotFoundException {
		securityContextManager.setEmailtohl();
		userService.getUserByEmail(customer.getEmail());
	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testMergeEmployee1() {
		SecurityContextHolder.clearContext();
		userService.mergeEmployee(employee.getEmail(), employee);
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testMergeEmployee2() {
		securityContextManager.setBar();
		userService.mergeEmployee(employee.getEmail(), employee);
	}
	
	@Test
	public void testMergeEmployee3() {
		securityContextManager.setFoo();
		userService.mergeEmployee(employee.getEmail(), employee);
		
		securityContextManager.setEmailtohl();
		userService.mergeEmployee(employee.getEmail(), employee);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testMergeCustomer1() {
		SecurityContextHolder.clearContext();
		userService.mergeCustomer(customer.getEmail(), customer);
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testMergeCustomer2() {
		securityContextManager.setBar();
		userService.mergeCustomer(customer.getEmail(), customer);
	}
	
	@Test
	public void testMergeCustomer3() {
		securityContextManager.setBaz();
		userService.mergeCustomer(customer.getEmail(), customer);
		
		securityContextManager.setEmailtohl();
		userService.mergeCustomer(customer.getEmail(), customer);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testUpdateIconSrc1() {
		securityContextManager.clearContext();
		userService.updateIconSrc(employeeId, "url");
	}
	
	@Test
	public void testUpdateIconSrc2() {
		securityContextManager.setFoo();
		userService.updateIconSrc(employeeId, "url");
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testUpdateIcon1() {
		securityContextManager.clearContext();
		userService.updateIcon(employeeId, new byte[1]);
	}
	
	@Test
	public void testUpdateIcon2() {
		securityContextManager.setFoo();
		userService.updateIcon(employeeId, new byte[1]);
	}
	
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testGetUserPager1() {
		SecurityContextHolder.clearContext();
		userService.getUserPager(null, null);
	}
	
	@Test
	public void testGetUserPager2() {
		securityContextManager.setBar();
		userService.getUserPager(customer, pageable);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testGetUserPage1() {
		SecurityContextHolder.clearContext();
		userService.getUserPage(customer, pageable);
	}
	
	@Test
	public void testGetUserPage2() {
		securityContextManager.setBar();
		userService.getUserPage(customer, pageable);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testGetRoles1() {
		securityContextManager.clearContext();
		userService.getRoles();
	}
	
	@Test
	public void testGetRoles2() {
		securityContextManager.setBaz();
		userService.getRoles();
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testSetPublicKey1() {
		securityContextManager.clearContext();
		userService.setPublicKey(td.baz.getEmail(), "eyJtb2R1bGUiOiI5MTIxMzkwMjU2NDM1ODA1MjM4NDg4MDI5NDE3MjgxMzIxNjk4NTYxMDk2NTcwNTE5NDc2OTM4NDQ4NDA1NzgxMjAyMDM4NzM1NzQwNDg0OTczODQ5NzU2MTIzNjE3MjQ1MzI1MzMzMTEzNDMwMzAwMjc4NjIyNjc2NjkwMDEzMzkxOTgxMjAyMTk2NzY5Mjg2MDc3NzMwODkwOTkxODIyMDMzNTk4NjQ1NjkwMzU1NzYxNTU3NjUwNjkwMzI1MTE2NTUzODQ3OTI0NTc5OTk1MTQwNDM0NDkyOTk3NDg0MDg1NjM5ODI2NjU4NzY1NDM4NTE3ODk0Mzg5NTc4NDg1ODYxNDMxMjY3Mzg0OTM3MDE1MzgyMjg2MzAzODYxOTU5NzcyOTA1OTQwNDUzNjMxNjA2OSIsInB1YmxpY0tleSI6IjY1NTM3In0=");
	}
	
	@Test
	public void testSetPublicKey2() {
		securityContextManager.setBaz();
		userService.setPublicKey(td.baz.getEmail(), "eyJtb2R1bGUiOiI5MTIxMzkwMjU2NDM1ODA1MjM4NDg4MDI5NDE3MjgxMzIxNjk4NTYxMDk2NTcwNTE5NDc2OTM4NDQ4NDA1NzgxMjAyMDM4NzM1NzQwNDg0OTczODQ5NzU2MTIzNjE3MjQ1MzI1MzMzMTEzNDMwMzAwMjc4NjIyNjc2NjkwMDEzMzkxOTgxMjAyMTk2NzY5Mjg2MDc3NzMwODkwOTkxODIyMDMzNTk4NjQ1NjkwMzU1NzYxNTU3NjUwNjkwMzI1MTE2NTUzODQ3OTI0NTc5OTk1MTQwNDM0NDkyOTk3NDg0MDg1NjM5ODI2NjU4NzY1NDM4NTE3ODk0Mzg5NTc4NDg1ODYxNDMxMjY3Mzg0OTM3MDE1MzgyMjg2MzAzODYxOTU5NzcyOTA1OTQwNDUzNjMxNjA2OSIsInB1YmxpY0tleSI6IjY1NTM3In0=");
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testDeletePublicKey1() {
		securityContextManager.clearContext();
		userService.clearPublicKey(td.baz.getEmail());
	}
	
	@Test
	public void testDeletePublicKey2() {
		securityContextManager.setBaz();
		userService.clearPublicKey(td.baz.getEmail());
	}

}
