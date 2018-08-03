package com.github.emailtohl.integration.web.service.cms;

import com.github.emailtohl.integration.web.service.cms.entities.Comment;
import com.github.emailtohl.lib.jpa.SearchInterface;
/**
 * 评论的搜索接口
 * @author HeLei
 */
interface CommentRepositoryCustomization extends SearchInterface<Comment, Long> {

}
