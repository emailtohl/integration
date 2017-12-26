package com.github.emailtohl.integration.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;

import com.github.emailtohl.integration.core.role.RoleAuditedService;
import com.github.emailtohl.integration.core.user.customer.CustomerAuditedService;
import com.github.emailtohl.integration.core.user.employee.EmployeeAuditedService;

/**
 * 控制层测试
 * @author HeLei
 */
public class AuditCtrlTest {
	MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		CustomerAuditedService customerAuditedService = Mockito.mock(CustomerAuditedService.class);
		EmployeeAuditedService employeeAuditedService = Mockito.mock(EmployeeAuditedService.class);
		RoleAuditedService roleAuditedService = Mockito.mock(RoleAuditedService.class);
		AuditCtrl ctrl = new AuditCtrl(customerAuditedService, employeeAuditedService, roleAuditedService);
		mockMvc = standaloneSetup(ctrl).build();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetRoleRevision() throws Exception {
		mockMvc.perform(get("/audit/role/777"))
		.andExpect(status().isOk());
	}

	@Test
	public void testGetRoleAtRevision() throws Exception {
		mockMvc.perform(get("/audit/role/777/revision/888"))
		.andExpect(status().isOk());
	}

	@Test
	public void testGetCustomerRevision() throws Exception {
		mockMvc.perform(get("/audit/customer/777"))
		.andExpect(status().isOk());
	}

	@Test
	public void testGetCustomerAtRevision() throws Exception {
		mockMvc.perform(get("/audit/customer/777/revision/888"))
		.andExpect(status().isOk());
	}

	@Test
	public void testGetEmployeeRevision() throws Exception {
		mockMvc.perform(get("/audit/employee/777"))
		.andExpect(status().isOk());
	}

	@Test
	public void testGetEmployeeAtRevision() throws Exception {
		mockMvc.perform(get("/audit/employee/777/revision/888"))
		.andExpect(status().isOk());
	}

}
