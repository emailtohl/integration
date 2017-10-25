package com.github.emailtohl.integration.web.controller;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import com.github.emailtohl.integration.common.exception.NotFoundException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.user.dto.UserDto;
import com.github.emailtohl.integration.user.entities.Role;
import com.github.emailtohl.integration.user.entities.User;
import com.github.emailtohl.integration.user.service.UserService;
import com.github.emailtohl.integration.web.WebTestData;
import com.google.gson.Gson;
/**
 * 本类中的测试并不依赖于Spring容器中的任何Bean
 * @author HeLei
 * @date 2017.02.04
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = ServiceConfiguration.class)
//@ActiveProfiles({ DataSourceConfiguration.H2_RAM_DB })
public class UserCtrlTest {
	private static final Logger logger = LogManager.getLogger();
	UserService userService = Mockito.mock(UserService.class, RETURNS_SMART_NULLS);
	Answer<Object> answer = invocation -> {
		logger.debug(invocation.getMethod());
		logger.debug(invocation.getArguments());
		return invocation.getMock();
	};
	Gson gson = new Gson();
	
	MockMvc mockMvc;
	MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    UserDto empDto = new UserDto(), cusDto = new UserDto();
    WebTestData td = new WebTestData();
    Pageable pageable = new PageRequest(0, 20);
    Page<User> page = new PageImpl<>(Arrays.asList());
	
    {
    	when(userService.exist(anyString())).thenReturn(true);
    	when(userService.addEmployee(anyObject())).thenReturn(td.bar);
    	when(userService.addCustomer(anyObject())).thenReturn(td.emailtohl);
    	when(userService.enableUser(anyLong())).thenReturn(td.emailtohl);
    	when(userService.disableUser(anyLong())).thenReturn(td.emailtohl);
    	when(userService.grantRoles(anyLong(), anyString())).thenReturn(td.emailtohl);
    	when(userService.grantUserRole(anyLong())).thenReturn(td.emailtohl);
    	when(userService.changePassword(anyString(), anyString())).thenReturn(td.emailtohl);
    	when(userService.changePasswordByEmail(anyString(), anyString())).thenReturn(td.emailtohl);
    	doAnswer(answer).when(userService).deleteUser(anyLong());
    	when(userService.getUser(anyLong())).thenReturn(td.emailtohl);
    	try {
			when(userService.getUserByEmail(anyString())).thenReturn(td.emailtohl);
		} catch (NotFoundException e) {
		}
    	when(userService.updateIconSrc(anyLong(), anyString())).thenReturn(td.emailtohl);
    	when(userService.updateIcon(anyLong(), anyObject())).thenReturn(td.emailtohl);
    	when(userService.mergeEmployee(anyString(), anyObject())).thenReturn(td.emailtohl);
    	when(userService.mergeCustomer(anyString(), anyObject())).thenReturn(td.emailtohl);
    	when(userService.getUserPage(anyObject(), anyObject())).thenReturn(new Paging<User>(page));
    	when(userService.isExist(anyString())).thenReturn(true);
    	when(userService.getPageByRoles(anyString(), anyObject(), anyObject())).thenReturn(new Paging<User>(page));
    	when(userService.getRoles()).thenReturn(Arrays.asList(td.admin, td.manager, td.employee));
    	when(userService.setPublicKey(anyString(), anyString())).thenReturn(td.emailtohl);
    	when(userService.clearPublicKey(anyString())).thenReturn(td.emailtohl);
    	
    }
    
	@Before
	public void setUp() {
		UserCtrl userCtrl = new UserCtrl();
		userCtrl.setUserService(userService);
		mockMvc = standaloneSetup(userCtrl).build();
		
		BeanUtils.copyProperties(td.bar, empDto);
		BeanUtils.copyProperties(td.baz, cusDto);
	}
	
	@Test
	public void testDiscover() throws Exception {
		mockMvc.perform(options("/user"))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()))
		.andExpect(header().stringValues("Allow", "OPTIONS,HEAD,GET"));
	}

	@Test
	public void testDiscoverLong() throws Exception {
		mockMvc.perform(options("/user/100"))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()))
		.andExpect(header().stringValues("Allow", "OPTIONS,HEAD,GET,PUT,DELETE"));
	}

	@Test
	public void testExist() throws Exception {
		mockMvc.perform(get("/user/exist?username=abc"))
		.andExpect(status().isOk());
	}
	
	@Test
	public void testGetUserById() throws Exception {
		mockMvc.perform(get("/user/id/100"))
		.andExpect(status().isOk());
		/* id为0会触发service层的约束异常，大于0不能确定是否存在User
		mockMvc.perform(get("/user/id/0"))
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()));*/
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetUserByEmail() throws Exception {
		mockMvc.perform(get("/user/email?email=" + empDto.getEmail()))
		.andExpect(status().isOk());
		
		String noExist = "aaa@test.com";
		when(userService.getUserByEmail(noExist)).thenThrow(NotFoundException.class);
		
		mockMvc.perform(get("/user/email?email=" + noExist))
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()));
	}
	
//	@Test
	public void testGetPageByRoles() throws Exception {
		mockMvc.perform(get("/user/pageByRoles?email=" + empDto.getEmail()))
		.andExpect(status().isOk());
	}

//	@Test
	public void testGetUserPage() throws Exception {
		mockMvc.perform(get("/user/page?email=foo@test.com&page=0&size=20"))
		.andExpect(status().isOk());
		
		mockMvc.perform(get("/user/email?email=bar@test.com&page=0&size=20"))
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()));
	}

	@Test
	public void testAddEmployee() throws Exception {
		mockMvc.perform(post("/user/employee")
		.characterEncoding("UTF-8")
        .contentType(MediaType.APPLICATION_JSON)  
        .content(gson.toJson(empDto).getBytes()))
		.andExpect(status().is(HttpStatus.CREATED.value()));
		
		mockMvc.perform(post("/user/employee")
		.characterEncoding("UTF-8")  
        .contentType(MediaType.APPLICATION_JSON)  
        .content("{username:\"foo\"}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}
	
	@Test
	public void testAddCustomer() throws Exception {
		mockMvc.perform(post("/user/customer")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(cusDto).getBytes()))
		.andExpect(status().is(HttpStatus.CREATED.value()));
		
		mockMvc.perform(post("/user/customer")
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{username:\"foo\"}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}
	
	@Test
	public void testEnableUser() throws Exception {
		mockMvc.perform(put("/user/enableUser/100")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}
	
	@Test
	public void testDisableUser() throws Exception {
		mockMvc.perform(put("/user/disableUser/100")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}

	@Test
	public void testGrantRoles() throws Exception {
		String[] authorities = new String[] { Role.MANAGER, Role.EMPLOYEE };
		mockMvc.perform(put("/user/grantRoles/100")
				.characterEncoding("UTF-8")
		        .contentType(MediaType.APPLICATION_JSON)  
		        .content(gson.toJson(authorities).getBytes()))
		.andExpect(status().isOk());
	}
	
	@Test
	public void testUpdateEmployee() throws Exception {
		mockMvc.perform(put("/user/employee/100")
		.characterEncoding("UTF-8")
        .contentType(MediaType.APPLICATION_JSON)  
        .content(gson.toJson(empDto).getBytes()))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
		
		mockMvc.perform(put("/user/employee/100")
		.characterEncoding("UTF-8")
        .contentType(MediaType.APPLICATION_JSON)  
        .content("{username:\"foo\"}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}
	
	@Test
	public void testUpdateCustomer() throws Exception {
		mockMvc.perform(put("/user/customer/100")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(cusDto).getBytes()))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
		
		mockMvc.perform(put("/user/customer/100")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{username:\"baz\"}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void testDelete() throws Exception {
		mockMvc.perform(delete("/user/100"))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
	}

	@Test
	public void testGetRoles() throws Exception {
		mockMvc.perform(get("/user/role"))
		.andExpect(status().isOk());
	}
	
//	@Test
	public void testUploadIcon() throws Exception {
		mockMvc.perform(fileUpload("/user/icon").param("id", "100")
				.characterEncoding("UTF-8"))
				.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
	}
}
