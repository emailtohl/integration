package com.github.emailtohl.integration.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.web.config.MockConfig;
import com.github.emailtohl.integration.web.config.WebTestData;
import com.google.gson.Gson;

/**
 * 控制层测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockConfig.class)
public class EmployeeCtrlTest {
	@Inject
	Gson gson;
	@Inject
	WebTestData td;
	@Inject
	EmployeeService employeeService;
	
	Long id;
	Integer empNum;
	Pageable pageable = new PageRequest(0, 20);
	MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCharacterEncoding("UTF-8");
	    MockHttpServletResponse response = new MockHttpServletResponse();
	    response.getContentLength();
	    
	    EmployeeCtrl ctrl = new EmployeeCtrl();
	    ctrl.setEmployeeService(employeeService);
	    mockMvc = standaloneSetup(ctrl)
	    		.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
	    		.build();
	    
	    // test create
	    Employee e = new Employee();
		e.setEmail("hello@world.com");
		e.setName("hello");
		e.setNickname("world");
		e.setPassword("123456");
		
	    String json = mockMvc.perform(post("/employee")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(e).getBytes()))
		.andExpect(status().is2xxSuccessful())
		.andReturn().getResponse().getContentAsString();
	    e = gson.fromJson(json, Employee.class);
	    id = e.getId();
	    empNum = e.getEmpNum();
	}

	@After
	public void tearDown() throws Exception {
		// test delete
		mockMvc.perform(delete("/employee/" + id))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testCreate() throws Exception {
		mockMvc.perform(post("/employee")
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{\"empNum\":0}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}
	
	@Test
	public void testExist() throws Exception {
		String json = mockMvc.perform(get("/employee/exist?cellPhoneOrEmail=" + td.bar.getEmail()))
		.andExpect(status().is2xxSuccessful())
		.andReturn().getResponse().getContentAsString();
		System.out.println(json);
	}

	@Test
	public void testGet() throws Exception {
		mockMvc.perform(get("/employee/" + id))
			.andExpect(status().isOk())
//			.andExpect(content().json(gson.toJson(td.bar)))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
		
		mockMvc.perform(get("/employee/" + Integer.MAX_VALUE))
		.andExpect(status().is4xxClientError());
	}

	@Test
	public void testQueryEmployeePageable() throws Exception {
		mockMvc.perform(get("/employee/page?empNum=1001&name=foo&page=1"))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testQueryEmployee() throws Exception {
		mockMvc.perform(get("/employee?name=foo"))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testUpdate() throws Exception {
		mockMvc.perform(put("/employee/" + id)
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(td.bar).getBytes()))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(put("/employee/" + id)
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{empNum:0}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void testSearch() throws Exception {
		String result = mockMvc.perform(get("/employee/search"))
		.andExpect(status().is2xxSuccessful())
		.andReturn().getResponse().getContentAsString();
		
		System.out.println(result);
		
		result = mockMvc.perform(get("/employee/search?query=foo"))
		.andExpect(status().is2xxSuccessful())
		.andReturn().getResponse().getContentAsString();
		
		System.out.println(result);
	}
	
	@Test
	public void testGetByEmpNum() throws Exception {
		mockMvc.perform(get("/employee/empNum/" + empNum))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(get("/employee/empNum/" + (empNum + 1)))
		.andExpect(status().isNotFound());
	}

	@Test
	public void testGrandRoles() throws Exception {
		EmployeeCtrl.Form form = new EmployeeCtrl.Form();
	    form.id = id;
		form.roleNames = new String[] {"admin", "guest"};
		mockMvc.perform(post("/employee/grandRoles")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testUpdatePassword() throws Exception {
		EmployeeCtrl.Form form = new EmployeeCtrl.Form();
	    form.id = id;
		form.empNum = empNum;
		form.oldPassword = "123456";
		form.newPassword = "654321";
		mockMvc.perform(post("/employee/updatePassword")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testResetPassword() throws Exception {
		EmployeeCtrl.Form form = new EmployeeCtrl.Form();
	    form.id = id;
	    mockMvc.perform(post("/employee/resetPassword")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testEnabled() throws Exception {
		EmployeeCtrl.Form form = new EmployeeCtrl.Form();
	    form.id = id;
		form.enabled = true;
		mockMvc.perform(post("/employee/enabled")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

}
