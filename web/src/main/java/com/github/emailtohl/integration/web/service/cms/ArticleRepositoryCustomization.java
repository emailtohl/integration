package com.github.emailtohl.integration.web.service.cms;

import java.util.Collection;
import java.util.Map;

import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.lib.jpa.SearchInterface;

/**
 * 文章的搜索接口
 * @author HeLei
 */
interface ArticleRepositoryCustomization extends SearchInterface<Article, Long> {
	/**
	 * 根据文章的id查询对应的评论数量
	 * @param ids
	 * @return
	 */
	Map<Long, Long> getCommentNumbers(Collection<Long> articleIds);
}
