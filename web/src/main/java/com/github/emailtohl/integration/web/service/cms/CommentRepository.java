package com.github.emailtohl.integration.web.service.cms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.web.service.cms.entities.Comment;

/**
 * 文章评论的数据访问接口
 * @author HeLei
 */
interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustomization {
	
	/**
	 * 查询有多少评论引用了本评论
	 * @param id
	 * @return
	 */
	List<Comment> findByCommentId(Long id);
	
	/**
	 * 计算某文章有多少评论
	 * @param articleId
	 * @return
	 */
	long countByArticleId(long articleId);
}
