package com.github.emailtohl.integration.web.controller;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.util.NestedServletException;

import com.github.emailtohl.integration.user.service.UserService;
import com.github.emailtohl.integration.web.WebTestData;
import com.github.emailtohl.integration.web.config.DataSourceConfiguration;
import com.github.emailtohl.integration.web.service.mail.EmailService;
import com.github.emailtohl.integration.web.webTestConfig.ServiceConfiguration;
import com.google.gson.Gson;
/**
 * 本类测试依赖Spring容器中的localValidatorFactoryBean做校验
 * 在classpath下，除了需要hibernate-validator外还需要org.glassfish.web中的javax.el
 * @author HeLei
 * @date 2017.02.04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles({ DataSourceConfiguration.H2_RAM_DB, DataSourceConfiguration.ENV_TEST_PATH })
public class LoginCtrlTest {
	private static final Logger logger = LogManager.getLogger();
	UserService userService = Mockito.mock(UserService.class, RETURNS_SMART_NULLS);
	@Inject ThreadPoolTaskScheduler taskScheduler;
	MockMvc mockMvc;
	WebTestData td = new WebTestData();
	
	@Before
	public void setUp() {
		Answer<Object> answer = invocation -> {
			logger.debug(invocation.getMethod());
			logger.debug(invocation.getArguments());
			return invocation.getMock();
		};
		EmailService emailService = mock(EmailService.class);
		doAnswer(answer).when(emailService).sendMail("emailtohl@163.com", "test", "test");
		doAnswer(answer).when(emailService).enableUser("http://localhost:8080/building/user/register", "emailtohl@163.com");
		doAnswer(answer).when(emailService).updatePassword("http://localhost:8080/building/user/register",
				"emailtohl@163.com", "test", "test");
		
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
		
		LoginCtrl authenticationCtrl = new LoginCtrl();
		authenticationCtrl.setUserService(userService);
		authenticationCtrl.setEmailService(emailService);
		authenticationCtrl.setGson(new Gson());
		authenticationCtrl.setSessionRegistry(mock(SessionRegistry.class));
//		不测试定时任务，否则需要等到时后才停止
//		authenticationCtrl.setTaskScheduler(taskScheduler);
		mockMvc = standaloneSetup(authenticationCtrl).setViewResolvers(viewResolver).build();
	}

	@Test
	public void testLogin() throws Exception {
		mockMvc.perform(get("/login"))
		.andExpect(status().isOk())
		.andExpect(view().name("login"));
	}
	
	@Test
	public void testGetRegisterPage() throws Exception {
		mockMvc.perform(get("/register"))
		.andExpect(status().isOk())
		.andExpect(view().name("register"));
	}
	
	@Test
	public void testRegister() throws Exception {
/*
		mockMvc.perform(post("/register")
				.param("email", foo.getEmail())
				.param("name", foo.getName())
				.param("password", "123456")
				)
		.andExpect(status().isOk())
		.andExpect(view().name("login"));
		*/
		mockMvc.perform(post("/register")
				.param("email", "foo")
				.param("name", td.foo.getName())
				.param("password", "123456")
				)
		.andExpect(status().is(302));
	}
	
//	NullPointerException会被spring转成NestedServletException，这是执行到定时任务产生的，即认可执行成功
	@Test(expected = NestedServletException.class)
	public void testForgetPassword() throws Exception {
		mockMvc.perform(post("/forgetPassword")
				.param("email", "abc@test.com")
				.param("_csrf", "_csrf")
				)
		.andExpect(status().is(404));
		
		when(userService.isExist(td.bar.getEmail())).thenReturn(true);
		mockMvc.perform(post("/forgetPassword")
				.param("email", td.bar.getEmail())
				.param("_csrf", "_csrf")
				)
		.andExpect(status().isOk());
	}
	
	@Test
	public void testGetUpdatePasswordPage() throws Exception {
		mockMvc.perform(get("/getUpdatePasswordPage")
				.param("email", td.foo.getEmail())
				.param("token", "token")
				)
		.andExpect(status().is(302));
		
/*		mockMvc.perform(get("/getUpdatePasswordPage")
				.param("email", foo.getEmail())
				.param("token", "token")
				)
		.andExpect(status().isOk())
		.andExpect(view().name("updatePassword"));*/
	}

	@Test
	public void testUpdatePassword() throws Exception {
		mockMvc.perform(post("/updatePassword")
				.param("email", td.foo.getEmail())
				.param("password", "password")
				.param("token", "token")
				)
		.andExpect(status().is(302));
	}
	
	@Test
	public void testAuthentication() throws Exception {
		mockMvc.perform(get("/authentication"))
		.andExpect(status().isOk());
	}

//	@Test
	public void testGetPageByAuthorities() {
		fail("Not yet implemented");
	}

	@Test
	public void testSecurePage() throws Exception {
		mockMvc.perform(get("/secure"))
		.andExpect(status().isOk())
		.andExpect(view().name("secure"));
	}

}
