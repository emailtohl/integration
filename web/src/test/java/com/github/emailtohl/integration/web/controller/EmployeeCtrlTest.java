package com.github.emailtohl.integration.web.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.web.WebTestData;
import com.github.emailtohl.integration.web.controller.EmployeeCtrl;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 控制层测试
 * @author HeLei
 */
public class EmployeeCtrlTest {
	private static final Logger logger = LogManager.getLogger();
	Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return false;
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			if (clazz == byte[].class) {
				return true;
			}
			return false;
		}
	})/* .setDateFormat(Constant.DATE_FORMAT) */.create();

	WebTestData td;
	final Long id = 777L;
	final Integer empNum = 8889;
	Pageable pageable = new PageRequest(0, 20);
	MockMvc mockMvc;
	EmployeeCtrl.Form form;

	@Before
	public void setUp() throws Exception {
		td = new WebTestData();
		Answer<Object> answer = invocation -> {
			logger.debug(invocation.getMethod());
			logger.debug(invocation.getArguments());
			return invocation.getMock();
		};
		EmployeeService employeeService = Mockito.mock(EmployeeService.class);
		td.bar.setId(id);
		when(employeeService.create(any())).thenReturn(td.bar);
		when(employeeService.get(id)).thenReturn(td.bar);
		when(employeeService.getByEmpNum(empNum)).thenReturn(td.bar);
		when(employeeService.update(id, td.bar)).thenReturn(td.bar);
		when(employeeService.grandRoles(anyLong(), anyVararg())).thenReturn(td.bar);
		when(employeeService.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(employeeService.enabled(anyLong(), anyBoolean())).thenReturn(td.bar);
		when(employeeService.updatePassword(any(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		doAnswer(answer).when(employeeService).delete(id);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCharacterEncoding("UTF-8");
	    MockHttpServletResponse response = new MockHttpServletResponse();
	    response.getContentLength();
	    EmployeeCtrl ctrl = new EmployeeCtrl();
	    ctrl.setEmployeeService(employeeService);
	    mockMvc = standaloneSetup(ctrl)
	    		.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
	    		.setViewResolvers(new ViewResolver() {
	                @Override
	                public View resolveViewName(String viewName, Locale locale) throws Exception {
	                    return new MappingJackson2JsonView();
	                }
	            })
	    		.build();
	    
	    form = ctrl.new Form();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreate() throws Exception {
		System.out.println(gson.toJson(td.baz));
		td.baz.setId(id);
		mockMvc.perform(post("/employee")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(td.baz).getBytes()))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(post("/employee")
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{empNum:0}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}
	
	@Test
	public void testExist() throws Exception {
		String json = mockMvc.perform(get("/employee/exist/" + td.bar.getEmpNum()))
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
		System.out.println(gson.toJson(td.baz));
		td.baz.setId(id);
		mockMvc.perform(put("/employee/" + id)
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(td.baz).getBytes()))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(put("/employee/" + id)
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{empNum:0}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void testDelete() throws Exception {
		mockMvc.perform(delete("/employee/" + id))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testSearch() throws Exception {
		mockMvc.perform(get("/employee/search"))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(get("/employee/search?query=foo"))
		.andExpect(status().is2xxSuccessful());
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
	    form.id = id;
	    mockMvc.perform(post("/employee/resetPassword")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testEnabled() throws Exception {
	    form.id = id;
		form.enabled = true;
		mockMvc.perform(post("/employee/enabled")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

}
