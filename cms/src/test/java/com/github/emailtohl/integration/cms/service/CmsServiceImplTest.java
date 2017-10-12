package com.github.emailtohl.integration.cms.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.emailtohl.integration.cms.CmsTestData;
import com.github.emailtohl.integration.cms.cmsTestConfig.DataSourceConfiguration;
import com.github.emailtohl.integration.cms.cmsTestConfig.ServiceConfiguration;
import com.github.emailtohl.integration.cms.dao.CleanAuditData;
import com.github.emailtohl.integration.cms.entities.Article;
import com.github.emailtohl.integration.cms.entities.Comment;
import com.github.emailtohl.integration.cms.entities.Type;
import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.common.jpa._Page;

/**
 * cms的服务层实现
 * @author HeLei
 * @data 2017.02.19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@ActiveProfiles(DataSourceConfiguration.DB_RAM_H2)
@Transactional
public class CmsServiceImplTest {
	static final Logger logger = LogManager.getLogger();
	final Pageable pageable = new PageRequest(0, 20);
	@Inject CmsService cmsService;
	@Inject CleanAuditData cleanAuditData;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFindArticle() throws ResourceNotFoundException {
		List<Article> ls = cmsService.recentArticles();
		Article a = ls.get(0);
		assertEquals(a, cmsService.getArticle(a.getId()));
	}

	@Test
	public void testFind() {
		// 从正文中搜索
		_Page<Article> p = cmsService.searchArticles("文章", pageable);
		logger.debug(p.getContent());
//		assertTrue(p.getTotalElements() > 0);
		
		// 从评论中搜索
		p = cmsService.searchArticles("评论", pageable);
		logger.debug(p.getContent());
//		assertTrue(p.getTotalElements() > 0);
	}

	@Test
	public void testArticle() throws ResourceNotFoundException {
		CmsTestData td = new CmsTestData();
		Article a = new Article("test", "test", "test", "summary");
		long id = cmsService.saveArticle(td.emailtohl.getEmail(), a, td.subType.getName()).getId();
		assertTrue(id > 0);
		a = cmsService.getArticle(id);
		try {
			assertEquals(td.emailtohl, a.getAuthor());
			assertEquals(td.subType, a.getType());
//			assertTrue(cmsService.findTypeByName(subType.getName()).getArticles().contains(a));
			
			cmsService.updateArticle(id, "update", null, "test body", "summary", td.parent.getName());
			a = cmsService.getArticle(id);
			assertEquals("update", a.getTitle());
			assertEquals(td.parent, a.getType());
			
			assertFalse(cmsService.findTypeByName(td.subType.getName()).getArticles().contains(a));
//			assertTrue(cmsService.findTypeByName(parent.getName()).getArticles().contains(a));
			
		} finally {
			cmsService.deleteArticle(id);
			cleanAuditData.cleanArticleAudit(id);
		}
	}

	@Test
	public void testComment() {
		CmsTestData td = new CmsTestData();
		Article a = new Article("test", "test", "test", "summary");
		long articleId = cmsService.saveArticle(td.emailtohl.getEmail(), a, "noType").getId();
		Article article = cmsService.saveComment(td.bar.getEmail(), articleId, "my comment");
		assertFalse(article.getComments().isEmpty());
		long commentId = article.getComments().get(0).getId();
		try {
			Comment c = cmsService.findComment(commentId);
			assertEquals("my comment", c.getContent());
			assertEquals(td.bar.getUsername(), c.getCritics());
			assertEquals(td.bar.getIconSrc(), c.getIcon());
			cmsService.updateComment(td.bar.getEmail(), commentId, "update");
			c = cmsService.findComment(commentId);
			assertEquals("update", c.getContent());
		} finally {
			cmsService.deleteComment(commentId);
			cmsService.deleteArticle(articleId);
			cleanAuditData.cleanArticleAudit(articleId);
		}
	}
	
	@Test
	public void testType() {
		CmsTestData td = new CmsTestData();
		long id = cmsService.saveType("testType", "testType", null);
		try {
			Type t = cmsService.findTypeByName("testType");
			assertNotNull(t);
			cmsService.updateType(id, "updateTestType", "updateTestType", td.parent.getName());
			t = cmsService.findTypeByName("updateTestType");
			assertEquals("updateTestType", t.getDescription());
			assertEquals(td.parent, t.getParent());
		} finally {
			cmsService.deleteType(id);
		}
	
	}

	@Test
	public void testRecentArticles() {
		List<Article> ls = cmsService.recentArticles();
		assertFalse(ls.isEmpty());
	}

	@Test
	public void testRecentComments() {
		List<Comment> ls = cmsService.recentComments();
		assertFalse(ls.isEmpty());
	}

	@Test
	public void testCommentCount() {
		List<Article> ls = cmsService.recentArticles();
		Article a = ls.get(0);
		assertTrue(cmsService.commentCount(a.getId()) > 0);
	}
	
	@Test
	public void testGetArticleTypes() {
		List<Type> ls = cmsService.getTypes();
		assertFalse(ls.isEmpty());
	}

	@Test
	public void testClassify() {
		Map<Type, List<Article>> categories = cmsService.classify();
		categories.entrySet().stream().forEach(e -> logger.debug(e));
		assertTrue(categories.size() > 0);
	}

}
