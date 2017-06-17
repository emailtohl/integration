package com.github.emailtohl.integration.cms.dao;

import com.github.emailtohl.integration.cms.entities.Article;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;

/**
 * 文章实体的数据访问层
 * 
 * @author HeLei
 * @date 2017.02.12
 */
public class ArticleRepositoryImpl extends AbstractSearchableRepository<Article>
		implements ArticleRepositoryCustomization {

}
