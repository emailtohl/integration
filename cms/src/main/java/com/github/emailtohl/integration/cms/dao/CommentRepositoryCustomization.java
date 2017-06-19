package com.github.emailtohl.integration.cms.dao;

import com.github.emailtohl.integration.cms.entities.Comment;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
/**
 * 评论的搜索接口
 * @author HeLei
 * @date 2017.03.05
 */
public interface CommentRepositoryCustomization extends SearchableRepository<Comment> {

}
