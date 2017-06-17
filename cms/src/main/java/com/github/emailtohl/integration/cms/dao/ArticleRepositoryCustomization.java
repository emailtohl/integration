package com.github.emailtohl.integration.cms.dao;

import com.github.emailtohl.integration.cms.entities.Article;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;

/**
 * 文章的搜索接口
 * @author HeLei
 * @date 2017.02.17
 */
public interface ArticleRepositoryCustomization extends SearchableRepository<Article> {

}
