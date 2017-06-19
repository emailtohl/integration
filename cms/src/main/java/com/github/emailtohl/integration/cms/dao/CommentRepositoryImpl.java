package com.github.emailtohl.integration.cms.dao;

import com.github.emailtohl.integration.cms.entities.Comment;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;

/**
 * 评论实体的数据访问层
 * 
 * @author HeLei
 * @date 2017.03.05
 */
public class CommentRepositoryImpl extends AbstractSearchableRepository<Comment>
		implements CommentRepositoryCustomization {

}
