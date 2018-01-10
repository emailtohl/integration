package com.github.emailtohl.integration.web.service.cms;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.inject.Inject;

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
import com.github.emailtohl.integration.web.WebTestConfig;
import com.github.emailtohl.integration.web.WebTestData;
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
	WebTestData td;
	@Inject
	Gson gson;
	
	Long typeId;
	Long articleId;

	@Before
	public void setUp() throws Exception {
		Type tp = new Type("test parent", "for test", null);
		tp = typeService.create(tp);
		typeId = tp.getId();
		Article a = new Article("文章名", "关键词", "正文", "概述");
		a.setType(tp);
		StandardService.CURRENT_USERNAME.set(td.baz.getEmail());
		a = articleService.create(a);
		articleId = a.getId();
	}

	@After
	public void tearDown() throws Exception {
		// 先删类型，测试文章的类型引用为空
		typeService.delete(typeId);
		Article a = articleService.get(articleId);
		assertNull(a.getType());
		articleService.delete(articleId);
	}

	@Test
	public void testQuery() {
		// exist
		assertTrue(articleService.exist("一篇测试文章"));
		// query
		Article params = new Article("文章名", "关键词", "正文", "概述");
		List<Article> ls = articleService.query(params);
		gson.toJson(ls);
		assertFalse(ls.isEmpty());

		Paging<Article> p = articleService.query(params, new PageRequest(0, 20));
		gson.toJson(p);
		assertFalse(p.getContent().isEmpty());
	}

}
