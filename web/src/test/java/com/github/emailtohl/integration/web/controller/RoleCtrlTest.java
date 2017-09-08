package com.github.emailtohl.integration.web.controller;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.github.emailtohl.integration.user.dto.RoleDto;
import com.github.emailtohl.integration.user.service.RoleService;
import com.github.emailtohl.integration.web.webTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.web.webTestConfig.ServiceConfiguration;
import com.google.gson.Gson;
/**
 * 本类中的测试并不依赖于Spring容器中的任何Bean
 * 但是要让SpringJUnit4ClassRunner运行必须指定一个配置
 * @author HeLei
 * @date 2017.02.04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles({ DataSourceConfiguration.DB_RAM_H2 })
public class RoleCtrlTest {
	Gson gson = new Gson();
	
	MockMvc mockMvc;
	MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    
    RoleDto r = new RoleDto();

	@Before
	public void setUp() throws Exception {
		r.setName("roleTest");
		r.setDescription("for test");
		r.getAuthorityNames().add("authorityTest");
		RoleCtrl ctrl = new RoleCtrl();
		RoleService roleService = mock(RoleService.class);
		ctrl.setRoleService(roleService);
		mockMvc = standaloneSetup(ctrl).build();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testExist() throws Exception {
		mockMvc.perform(get("/role/exist?roleName=abc"))
		.andExpect(status().isOk());
	}

	@Test
	public void testGetRole() throws Exception {
		mockMvc.perform(get("/role/100"))
		.andExpect(status().isOk());
	}

	@Test
	public void testGetRoles() throws Exception {
		mockMvc.perform(get("/role"))
		.andExpect(status().isOk());
	}

	@Test
	public void testGetAuthorities() throws Exception {
		mockMvc.perform(get("/authority"))
		.andExpect(status().isOk());
	}

	@Test
	public void testCreateRole() throws Exception {
		mockMvc.perform(post("/role")
				.characterEncoding("UTF-8")
		        .contentType(MediaType.APPLICATION_JSON)  
		        .content(gson.toJson(r).getBytes()))
				.andExpect(status().is(HttpStatus.CREATED.value()));
		
		mockMvc.perform(post("/role")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(new RoleDto()).getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void testUpdateRole() throws Exception {
		mockMvc.perform(put("/role/100")
				.characterEncoding("UTF-8")
		        .contentType(MediaType.APPLICATION_JSON)  
		        .content(gson.toJson(r).getBytes()))
				.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
	}

	@Test
	public void testGrantAuthorities() throws Exception {
		mockMvc.perform(put("/role/100/authorityNames/test1,test2")
				.characterEncoding("UTF-8")
		        .contentType(MediaType.APPLICATION_JSON)  
		        .content(gson.toJson(r).getBytes()))
				.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
	}

	@Test
	public void testDeleteRole() throws Exception {
		mockMvc.perform(delete("/role/100"))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
	}

}
