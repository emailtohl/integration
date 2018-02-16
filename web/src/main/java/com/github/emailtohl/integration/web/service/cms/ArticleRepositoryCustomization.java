package com.github.emailtohl.integration.web.service.cms;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.web.service.cms.entities.Article;

/**
 * 文章的搜索接口
 * @author HeLei
 */
interface ArticleRepositoryCustomization extends SearchableRepository<Article> {
}
