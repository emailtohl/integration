package com.github.emailtohl.integration.web.service.cms;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;

/**
 * 文章的评论接口
 * @author HeLei
 */
public interface CommentService {
	
	/**
	 * 创建一个评论，评论应该基于文章或另一篇评论来创建
	 * @param entity
	 * @return
	 * @throws NotAcceptableException 若被评论的对象关闭了评论，则抛出不能接受的异常
	 */
	Comment create(Comment entity) throws NotAcceptableException;

	/**
	 * 获取评论
	 * @param id
	 * @return
	 */
	Comment get(Long id);
	
	/**
	 * 全文搜索评论
	 * @param query
	 * @param pageable
	 * @return
	 */
	Paging<Comment> search(String query, Pageable pageable);
	
	/**
	 * 查询评论
	 * @param params
	 * @param pageable
	 * @return
	 */
	Paging<Comment> query(Comment params, Pageable pageable);

	/**
	 * 查询评论
	 * @param params
	 * @return
	 */
	List<Comment> query(Comment params);

	/**
	 * 更新评论
	 * @param id
	 * @param newEntity
	 * @return
	 */
	Comment update(Long id, Comment newEntity);

	/**
	 * 删除评论
	 * @param id
	 */
	void delete(Long id);
	
	/**
	 * 同意还是拒绝评论发布
	 * @param id
	 * @param approved 同意还是拒绝
	 */
	Comment approve(long id, boolean approved);
	
	/**
	 * 让文章是否能被评论
	 * @param id
	 * @param canComment
	 * @return
	 */
	Comment canComment(Long id, boolean canComment);
	
	/**
	 * 供前端访问的接口，最近评论列表
	 * @return
	 */
	List<Comment> recentComments();
}
