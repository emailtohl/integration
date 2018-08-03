package com.github.emailtohl.integration.web.service.cms;

import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.lib.jpa.SearchRepository;

/**
 * 评论实体的数据访问层
 * @author HeLei
 */
class CommentRepositoryImpl extends SearchRepository<Comment, Long>
		implements CommentRepositoryCustomization {

}
