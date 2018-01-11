package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.WebTestConfig;
import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Type;
import com.google.gson.Gson;

/**
 * 测试
 * @author HeLei
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebTestConfig.class)
@ActiveProfiles({ DB_RAM_H2, ENV_NO_SERVLET })
public class ArticleServiceImplTest {
	@Inject
	TypeService typeService;
	@Inject
	ArticleService articleService;
	@Inject
	IdentityService identityService;
	@Inject
	FormService formService;
	@Inject
	@Named("presetData")
	CorePresetData pd;
	@Inject
	Gson gson;
	@Inject
	WebPresetData webPresetData;
	
	Long typeId;
	Long articleId;

	@Before
	public void setUp() throws Exception {
		Type tp = new Type("test parent", "for test", null);
		tp = typeService.create(tp);
		typeId = tp.getId();
		Article a = new Article("文章名", "关键词", "正文", "概述");
		a.setType(tp);
		StandardService.CURRENT_USERNAME.set(pd.user_emailtohl.getEmail());
		identityService.setAuthenticatedUserId(pd.user_emailtohl.getId().toString());
		a = articleService.create(a);
		articleId = a.getId();
	}

	@After
	public void tearDown() throws Exception {
		// 先删类型，测试文章的类型引用为空
		typeService.delete(typeId);
		Article a = articleService.get(articleId);
		assertEquals(webPresetData.unclassified, a.getType());
		articleService.delete(articleId);
	}

	@Test
	public void testQuery() {
		// exist
		assertTrue(articleService.exist("一篇测试文章"));
		// query
		Article params = new Article("文章名", "关键词", "正文", "概述");
		List<Article> ls = articleService.query(params);
		System.out.println(gson.toJson(ls));
		assertFalse(ls.isEmpty());

		Paging<Article> p = articleService.query(params, new PageRequest(0, 20));
		System.out.println(gson.toJson(p));
		assertFalse(p.getContent().isEmpty());
	}
	
	@Test
	public void testUpdate() {
		Type other = new Type("other", "for other test", null);
		try {
			other = typeService.create(other);
			Article update = new Article("update", "update", "update", "update");
			update.setType(other);
			update = articleService.update(articleId, update);
			assertEquals(other, update.getType());
		} finally {
			typeService.delete(other.getId());
		}
	}

}
