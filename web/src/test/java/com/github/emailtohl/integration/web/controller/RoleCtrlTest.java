package com.github.emailtohl.integration.web.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Arrays;
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

import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.web.WebTestData;
import com.google.gson.Gson;

/**
 * 控制层测试
 * @author HeLei
 */
public class RoleCtrlTest {
	private static final Logger logger = LogManager.getLogger();
	Gson gson = new Gson();
	WebTestData td;
	final Long id = 777L;
	Pageable pageable = new PageRequest(0, 20);
	MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		td = new WebTestData();
		Answer<Object> answer = invocation -> {
			logger.debug(invocation.getMethod());
			logger.debug(invocation.getArguments());
			return invocation.getMock();
		};
		RoleService roleService = Mockito.mock(RoleService.class);
		when(roleService.create(any())).thenReturn(td.role_guest);
		when(roleService.get(id)).thenReturn(td.role_guest);
		when(roleService.update(id, td.role_guest)).thenReturn(td.role_guest);
		when(roleService.exist(eq(td.role_staff.getName()))).thenReturn(true);
		when(roleService.getAuthorities()).thenReturn(Arrays.asList(td.auth_customer_lock, td.auth_customer));
		doAnswer(answer).when(roleService).delete(id);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCharacterEncoding("UTF-8");
	    MockHttpServletResponse response = new MockHttpServletResponse();
	    response.getContentLength();
	    RoleCtrl ctrl = new RoleCtrl();
	    ctrl.setRoleService(roleService);
	    mockMvc = standaloneSetup(ctrl)
	    		.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
	    		.setViewResolvers(new ViewResolver() {
	                @Override
	                public View resolveViewName(String viewName, Locale locale) throws Exception {
	                    return new MappingJackson2JsonView();
	                }
	            })
	    		.build();
	    
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testCreate() throws Exception {
		mockMvc.perform(post("/role")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(td.role_guest).getBytes()))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(post("/role")
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{name:null}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void testGet() throws Exception {
		mockMvc.perform(get("/role/" + id))
			.andExpect(status().isOk())
//			.andExpect(content().json(gson.toJson(td.bar)))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
		
		mockMvc.perform(get("/role/" + Integer.MAX_VALUE))
		.andExpect(status().is4xxClientError());
	}

	@Test
	public void testQueryRolePageable() throws Exception {
		mockMvc.perform(get("/role/page?name=foo&page=1"))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testQueryRole() throws Exception {
		mockMvc.perform(get("/role?name=foo"))
		.andExpect(status().is2xxSuccessful());
	}
	
	@Test
	public void testGetAuthorities() throws Exception {
		String json = mockMvc.perform(get("/authority"))
		.andExpect(status().is2xxSuccessful())
		.andReturn().getResponse().getContentAsString();
		System.out.println(json);
	}

	@Test
	public void testUpdate() throws Exception {
		System.out.println(gson.toJson(td.role_guest));
		td.role_guest.setId(id);
		mockMvc.perform(put("/role/" + id)
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(td.role_guest).getBytes()))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(put("/role/" + id)
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{name:null}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void testDelete() throws Exception {
		mockMvc.perform(delete("/role/" + id))
		.andExpect(status().is2xxSuccessful());
	}
	
	@Test
	public void testExist() throws Exception {
		mockMvc.perform(get("/role/exist?roleName=" + td.role_staff.getName()))
		.andExpect(status().is2xxSuccessful());
	}

}
