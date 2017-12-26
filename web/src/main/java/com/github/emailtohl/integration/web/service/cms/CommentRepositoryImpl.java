package com.github.emailtohl.integration.web.service.cms;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;

/**
 * 评论实体的数据访问层
 * @author HeLei
 */
class CommentRepositoryImpl extends AbstractSearchableRepository<Comment>
		implements CommentRepositoryCustomization {

}
