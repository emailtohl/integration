package com.github.emailtohl.integration.web.service.cms;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.web.service.cms.entities.Article;

/**
 * 文章实体的数据访问接口
 * @author HeLei
 */
interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustomization {

	Page<Article> findByTypeName(String typeName, Pageable pageable);
}
