package com.github.emailtohl.integration.web.controller;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.*;

import java.io.IOException;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.exception.NotFoundException;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.config.WebTestConfig;
import com.github.emailtohl.integration.web.service.cms.ArticleService;
import com.github.emailtohl.integration.web.service.cms.CommentService;
import com.github.emailtohl.integration.web.service.cms.TypeService;
import com.github.emailtohl.integration.web.service.cms.entities.Article;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
/**
 * 内容控制器的测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestConfig.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class CmsCtrlTest {
	@Inject
	Configuration cfg;
	@Inject
	TypeService typeService;
	@Inject
	ArticleService articleService;
	@Inject
	CommentService commentService;
	@Inject
	CorePresetData cpd;
	@Inject
	WebPresetData wpd;
	
	CmsCtrl ctrl;
	
	Long articleId;

	@Before
	public void setUp() throws Exception {
		ctrl = new CmsCtrl(cfg, typeService, articleService, commentService);
		
		Article a = new Article("文章名", "关键词", "正文", "概述");
		a.setType(wpd.unclassified);
		StandardService.CURRENT_USER_ID.set(cpd.user_anonymous.getId());
		StandardService.CURRENT_USERNAME.set(cpd.user_anonymous.getEmail());
		a = articleService.create(a);
		articleId = a.getId();
	}

	@After
	public void tearDown() throws Exception {
		articleService.delete(articleId);
	}

	@Test
	public void testGetWebPage() throws TemplateException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ctrl.getWebPage(request, response);
		assertTrue(StringUtils.hasText(response.getContentAsString()));
	}

	@Test
	public void testGetDetail() throws NotFoundException, TemplateException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ctrl.getDetail(articleId, request, response);
		assertTrue(StringUtils.hasText(response.getContentAsString()));
	}

}
