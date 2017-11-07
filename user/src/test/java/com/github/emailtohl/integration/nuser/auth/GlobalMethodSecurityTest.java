package com.github.emailtohl.integration.nuser.auth;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
		scm.setEmailtohl();
		roleService.getAuthorities();
		scm.setBar();
		roleService.getAuthorities();
	}

}
