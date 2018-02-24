package com.github.emailtohl.integration.web.service.cms;

import java.util.Collection;
import java.util.Map;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.web.service.cms.entities.Article;

/**
 * 文章的搜索接口
 * @author HeLei
 */
interface ArticleRepositoryCustomization extends SearchableRepository<Article> {
	/**
	 * 根据文章的id查询对应的评论数量
	 * @param ids
	 * @return
	 */
	Map<Long, Integer> getCommentNumbers(Collection<Long> articleIds);
}
