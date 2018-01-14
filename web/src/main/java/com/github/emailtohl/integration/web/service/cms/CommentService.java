package com.github.emailtohl.integration.web.service.cms;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;

/**
 * 文章的评论接口
 * @author HeLei
 */
public interface CommentService {
	
	Comment create(Comment entity);

	Comment get(Long id);
	
	Paging<Comment> search(String query, Pageable pageable);
	
	Paging<Comment> query(Comment params, Pageable pageable);

	List<Comment> query(Comment params);

	Comment update(Long id, Comment newEntity);

	void delete(Long id);
	
	/**
	 * 同意还是拒绝评论发布
	 * @param id
	 * @param approved 同意还是拒绝
	 */
	Comment approve(long id, boolean approved);
	
	/**
	 * 供前端访问的接口，最近评论列表
	 * @return
	 */
	List<Comment> recentComments();
}
