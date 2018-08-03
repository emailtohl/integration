package com.github.emailtohl.integration.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Locale;

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
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.role.RoleType;
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
public class RoleCtrlTest {
	@Inject
	Gson gson;
	@Inject
	WebTestData td;
	@Inject
	RoleService roleService;
	
	Long id;
	Pageable pageable = PageRequest.of(0, 20);
	MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
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
	    Role entity = new Role("test", RoleType.CUSTOMER, "for test");
	    entity = roleService.create(entity);
	    id = entity.getId();
	}

	@After
	public void tearDown() throws Exception {
		roleService.delete(id);
	}

	@Test
	public void testCreate() throws Exception {
		mockMvc.perform(post("/role")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(td.pd.role_guest).getBytes()))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(post("/role")
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{name:null}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
		
		MockMvc empMockMvc = standaloneSetup(new EmployeeCtrl()).build();
		Employee emp = new Employee();
		emp.setName("福");
		emp.setEmail("foo@localhost");
		emp.setCellPhone("777777728888888");
		emp.setPassword("123456");
		emp.setDescription("for test");
		emp.setPost("dev");
		empMockMvc.perform(post("/employee")
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(emp).getBytes()))
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
		System.out.println(gson.toJson(td.pd.role_guest));
		td.pd.role_guest.setId(id);
		mockMvc.perform(put("/role/" + id)
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(td.pd.role_guest).getBytes()))
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
		mockMvc.perform(get("/role/exist?roleName=" + td.pd.role_staff.getName()))
		.andExpect(status().is2xxSuccessful());
	}

}
