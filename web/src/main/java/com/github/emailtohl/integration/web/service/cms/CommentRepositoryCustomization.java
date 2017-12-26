package com.github.emailtohl.integration.web.service.cms;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;
/**
 * 评论的搜索接口
 * @author HeLei
 */
interface CommentRepositoryCustomization extends SearchableRepository<Comment> {

}
