package com.github.emailtohl.integration.web.service.cms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.config.WebPresetData;
import com.github.emailtohl.integration.web.config.WebTestData;
import com.github.emailtohl.integration.web.config.WebTestEnvironment;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.lib.exception.NotAcceptableException;
import com.github.emailtohl.lib.jpa.Paging;
import com.google.gson.Gson;

/**
 * 测试
 * @author HeLei
 */
public class CommentServiceImplTest extends WebTestEnvironment {
	@Inject
	CommentService commentService;
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
	
	Long articleId;
	Long commentId1, commentId2, commentId3;
	
	@Before
	public void setUp() throws Exception {
		Article a = new Article("文章名", "关键词", "正文", "概述");
		a.setType(webPresetData.unclassified);
		StandardService.CURRENT_USER_ID.set(td.baz.getId());
		StandardService.CURRENT_USERNAME.set(td.baz.getEmail());
		identityService.setAuthenticatedUserId(td.baz.getId().toString());
		a = articleService.create(a);
		articleId = a.getId();
		
		Comment c1 = new Comment(), c2 = new Comment(), c3 = new Comment();
		c1.setContent("针对文章的评论1");
		c1.setArticle(a);

		c2.setContent("针对文章的评论2");
		c2.setArticle(a);
		
		c3.setContent("针对评论1的评论3");
		c3.setComment(c1);
		
		c1 = commentService.create(c1);
		c2 = commentService.create(c2);
		c3 = commentService.create(c3);
		
		commentId1 = c1.getId();
		commentId2 = c2.getId();
		commentId3 = c3.getId();
	}

	@After
	public void tearDown() throws Exception {
		Article a = articleService.get(articleId);
		assertEquals(webPresetData.unclassified, a.getType());
		// 测试先删除文章，再删除评论
		articleService.delete(articleId);
		
		commentService.delete(commentId1);
		commentService.delete(commentId2);
		commentService.delete(commentId3);
		
		StandardService.CURRENT_USER_ID.remove();
		StandardService.CURRENT_USERNAME.remove();
	}

	@Test
	public void testQueryCommentPageable() {
		Pageable pageable = PageRequest.of(0, 20);
		Comment params = new Comment();
		params.setContent("针对文章的评论1");
		params.setApprover(pd.user_bot.getEmployeeRef());
		params.setReviewer(td.baz.getCustomerRef());
		Paging<Comment> p = commentService.query(params, pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		params = new Comment();
		params.setContent("针对评论1的评论3");
		params.setApprover(pd.user_bot.getEmployeeRef());
		params.setReviewer(td.baz.getCustomerRef());
		p = commentService.query(params, pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		List<Comment> ls = commentService.query(params);
		assertFalse(ls.isEmpty());
		System.out.println(gson.toJson(ls));
		
		p = commentService.search("针对文章的评论2", pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
		
		p = commentService.search(td.baz.getEmail(), pageable);
		assertFalse(p.getContent().isEmpty());
		System.out.println(gson.toJson(p));
	}

	@Test
	public void testUpdateLongComment() {
		Comment c = new Comment();
		c.setContent("update");
		c = commentService.update(commentId2, c);
		c = commentService.get(commentId2);
		assertEquals("update", c.getContent());
		
		c = commentService.approve(commentId2, false);
		final Comment copy = (Comment) c.clone();
		boolean f = commentService.recentComments().stream().anyMatch(cm -> cm.equals(copy));
		assertFalse(f);
		
		c = commentService.approve(commentId2, true);
		commentService.canComment(commentId2, false);
		
		Comment newComment = new Comment();
		newComment.setContent("针对评论2的一个新的评论");
		newComment.setComment(c);
		boolean exec = false;
		try {
			commentService.create(newComment);
		} catch (NotAcceptableException e) {
			exec = true;
		}
		assertTrue(exec);
	}

}
