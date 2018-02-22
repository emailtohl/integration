package com.github.emailtohl.integration.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.web.config.MockConfig;
import com.github.emailtohl.integration.web.config.WebTestData;
import com.github.emailtohl.integration.web.service.mail.EmailService;
import com.google.gson.Gson;

/**
 * 控制层测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockConfig.class)
public class CustomerCtrlTest {
	@Inject
	Gson gson;
	@Inject
	WebTestData td;
	@Inject
	CustomerService customerService;
	
	Long id;
	final String token = "a_token";
	String cellPhoneOrEmail;
	Pageable pageable = new PageRequest(0, 20);
	MockMvc mockMvc;

	@Before
	public void setUp() throws Exception {
		if (new Random().nextBoolean()) {
			cellPhoneOrEmail = td.baz.getEmail();
		} else {
			cellPhoneOrEmail = td.baz.getCellPhone();
		}
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCharacterEncoding("UTF-8");
	    MockHttpServletResponse response = new MockHttpServletResponse();
	    response.getContentLength();
	    
	    CustomerCtrl ctrl = new CustomerCtrl();
	    ctrl.setCustomerService(customerService);
	    EmailService emailService = Mockito.mock(EmailService.class);
	    ctrl.setEmailService(emailService);
	    mockMvc = standaloneSetup(ctrl)
	    		.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
	    		.setViewResolvers(new ViewResolver() {
	                @Override
	                public View resolveViewName(String viewName, Locale locale) throws Exception {
	                    return new MappingJackson2JsonView();
	                }
	            })
	    		.build();
	    
	    Customer c = new Customer();
		c.setEmail("hello@world.com");
		c.setName("hello");
		c.setNickname("world");
		c.setPassword("123456");
		String json = mockMvc.perform(post("/customer")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(c).getBytes()))
		.andExpect(status().is2xxSuccessful())
		.andReturn().getResponse().getContentAsString();
	    c = gson.fromJson(json, Customer.class);
	    id = c.getId();
	}

	@After
	public void tearDown() throws Exception {
		mockMvc.perform(delete("/customer/" + id))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testCreate() throws Exception {
		mockMvc.perform(post("/customer")
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{\"identification\":\"123\"}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}
	
	@Test
	public void testExist() throws Exception {
		String json = mockMvc.perform(get("/customer/exist/" + td.baz.getEmail()))
		.andExpect(status().is2xxSuccessful())
		.andReturn().getResponse().getContentAsString();
		System.out.println(json);
	}
	
	@Test
	public void testGet() throws Exception {
		mockMvc.perform(get("/customer/" + id))
			.andExpect(status().isOk())
//			.andExpect(content().json(gson.toJson(td.baz)))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
		
		mockMvc.perform(get("/customer/" + Integer.MAX_VALUE))
		.andExpect(status().is4xxClientError());
	}

	@Test
	public void testQueryCustomerPageable() throws Exception {
		mockMvc.perform(get("/customer/page?empNum=1001&name=foo&page=1"))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testQueryCustomer() throws Exception {
		mockMvc.perform(get("/customer?name=foo"))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testUpdate() throws Exception {
		System.out.println(gson.toJson(td.baz));
		td.baz.setId(id);
		mockMvc.perform(put("/customer/" + id)
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(td.baz).getBytes()))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(put("/customer/" + id)
				.characterEncoding("UTF-8")  
				.contentType(MediaType.APPLICATION_JSON)  
				.content("{\"identification\":\"123\"}".getBytes()))
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void testSearch() throws Exception {
		mockMvc.perform(get("/customer/search"))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(get("/customer/search?query=foo"))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testGetByCellPhoneOrEmail() throws Exception {
		mockMvc.perform(get("/customer/cellPhoneOrEmail/" + cellPhoneOrEmail))
		.andExpect(status().is2xxSuccessful());
		
		mockMvc.perform(get("/customer/cellPhoneOrEmail/" + "abc@test.com"))
		.andExpect(status().isNotFound());
	}

	@Test
	public void testGrandRoles() throws Exception {
		CustomerCtrl.Form form = new CustomerCtrl.Form();
	    form.id = id;
		form.roleNames = new String[] {"admin", "guest"};
		mockMvc.perform(post("/customer/grandRoles")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testGetToken() throws Exception {
		mockMvc.perform(get("/customer/token?_csrf=a_csrf&cellPhoneOrEmail=" + cellPhoneOrEmail))
				.andExpect(status().is2xxSuccessful());
	}
	
	@Test
	public void testUpdatePassword() throws Exception {
		CustomerCtrl.Form form = new CustomerCtrl.Form();
		form.id = id;
		form.cellPhoneOrEmail = cellPhoneOrEmail;
		form.newPassword = "654321";
		form.token = token;
		mockMvc.perform(post("/customer/updatePassword")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testResetPassword() throws Exception {
		CustomerCtrl.Form form = new CustomerCtrl.Form();
	    form.id = id;
	    mockMvc.perform(post("/customer/resetPassword")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testEnabled() throws Exception {
		CustomerCtrl.Form form = new CustomerCtrl.Form();
	    form.id = id;
		form.enabled = true;
		mockMvc.perform(post("/customer/enabled")
				.characterEncoding("UTF-8")
				.contentType(MediaType.APPLICATION_JSON)  
				.content(gson.toJson(form).getBytes()))
		.andExpect(status().is2xxSuccessful());
	}

}
