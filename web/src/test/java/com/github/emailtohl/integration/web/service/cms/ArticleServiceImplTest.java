package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.config.WebTestConfig;
import com.github.emailtohl.integration.web.config.WebTestData;
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
	Gson gson;
	@Inject
	CorePresetData pd;
	@Inject
	WebPresetData webPresetData;
	@Inject
	WebTestData td;
	
	Long typeId;
	Long articleId;

	@Before
	public void setUp() throws Exception {
		Type tp = new Type("test parent", "for test", null);
		tp = typeService.create(tp);
		typeId = tp.getId();
		Article a = new Article("文章名", "关键词", "正文", "概述");
		a.setType(tp);
		StandardService.CURRENT_USER_ID.set(td.baz.getId());
		StandardService.CURRENT_USERNAME.set(td.baz.getEmail());
		identityService.setAuthenticatedUserId(td.baz.getId().toString());
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
		
		StandardService.CURRENT_USER_ID.remove();
		StandardService.CURRENT_USERNAME.remove();
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

		Pageable pageable = new PageRequest(0, 20);
		Paging<Article> p = articleService.query(params, pageable);
		System.out.println(gson.toJson(p));
		assertFalse(p.getContent().isEmpty());
		
		// 全文搜索
		p = articleService.search("文章名", pageable);
		assertFalse(p.getContent().isEmpty());
		p = articleService.search("关键词", pageable);
		assertFalse(p.getContent().isEmpty());
		p = articleService.search("正文", pageable);
		assertFalse(p.getContent().isEmpty());
		p = articleService.search("概述", pageable);
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
			
			// test approve and front read
			update = articleService.approve(articleId, false);
			assertFalse(update.getCanApproved());
			
			// 测试前端不能读取，同时测试articleClassify、readArticle两个方法
			boolean isExec = false;
			try {
				articleService.readArticle(articleId);
			} catch (NotAcceptableException e) {
				isExec = true;
			}
			assertTrue(isExec);
			
			final Article dontRead = update;
			boolean isMatch = articleService.articleClassify().values().stream().anyMatch(a -> a.contains(dontRead));
			assertFalse(isMatch);
			
			update = articleService.approve(articleId, true);
			assertTrue(update.getCanApproved());
			update = articleService.readArticle(articleId);
			assertNotNull(update);
			
		} finally {
			typeService.delete(other.getId());
		}
	}

}
