package com.github.emailtohl.integration.web.controller;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.github.emailtohl.integration.conference.dto.ForumPostDto;
import com.github.emailtohl.integration.conference.service.ForumPostService;
import com.github.emailtohl.integration.user.dto.UserDto;
import com.github.emailtohl.integration.web.WebTestData;
import com.github.emailtohl.integration.web.webTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.web.webTestConfig.ServiceConfiguration;
import com.google.gson.Gson;
/**
 * 本类测试依赖user模块下的认证服务
 * @author HeLei
 * @date 2017.02.04
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles({ DataSourceConfiguration.DB_RAM_H2 })
public class ForumPostCtrlTest {
	MockMvc mockMvc;
	MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    Pageable pageable = new PageRequest(0, 20);
    
    @Inject
    @Named("authenticationManagerImpl")
    AuthenticationManager authenticationManager;
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() {
		ForumPostService forumPostService = mock(ForumPostService.class);
		Answer<Object> answer = invocation -> {
			Object[] args = invocation.getArguments();
			return "called with arguments: " + args;
		};
		when(forumPostService.search(null, pageable)).thenReturn(null);
		when(forumPostService.getPage(pageable)).thenReturn(null);
		when(forumPostService.findForumPostByTitle(null)).thenReturn(null);
		doAnswer(answer).when(forumPostService).save(null, null, null, null);
		doAnswer(answer).when(forumPostService).delete(100L);
		ForumPostCtrl ctrl = new ForumPostCtrl();
		ctrl.setForumPostService(forumPostService);
		mockMvc = standaloneSetup(ctrl).build();
	}
	@Test
	public void testAdd() throws Exception {
		WebTestData td = new WebTestData();
		UserDto u = new UserDto();
		u.setEmail(td.foo.getEmail());
		u.setName(td.foo.getName());
		ForumPostDto dto = new ForumPostDto();
		dto.setEmail(td.foo.getEmail());
		dto.setUser(u);
		dto.setTitle("hello world");
		dto.setBody("hello world");
		dto.setKeywords("hello world");
		
		Authentication token = new UsernamePasswordAuthenticationToken(td.foo.getEmail(), "123456");
		Authentication authentication = authenticationManager.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		mockMvc.perform(post("/forum")
				.characterEncoding("UTF-8")  
		        .contentType(MediaType.APPLICATION_JSON)
		        .content(new Gson().toJson(dto)))
				.andExpect(status().isOk());
	}

//	@Test
	public void testSearch() throws Exception {
		mockMvc.perform(get("/forum/search?query=first"))
		.andExpect(status().isOk());
	}

//	@Test
	public void testGetPage() {
		fail("Not yet implemented");
	}

	@Test
	public void testDelete() throws Exception {
		mockMvc.perform(delete("/forum/" + 100))
		.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
	}
}
