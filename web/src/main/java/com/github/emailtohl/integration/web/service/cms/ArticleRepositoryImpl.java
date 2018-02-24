package com.github.emailtohl.integration.web.service.cms;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Comment;

/**
 * 文章实体的数据访问层
 * 
 * @author HeLei
 */
class ArticleRepositoryImpl extends AbstractSearchableRepository<Article>
		implements ArticleRepositoryCustomization {

	/*
	SELECT 
		A.ID,
		COUNT ( C.article_id ) 
	FROM
		article A LEFT JOIN article_comment C ON A.ID = C.article_id 
	GROUP BY
		A.ID,
		C.article_id
	 */
	@Override
	public Map<Long, Long> getCommentNumbers(Collection<Long> articleIds) {
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> q = b.createTupleQuery();
		Root<Article> r = q.from(Article.class);
		Join<Article, Comment> join = r.join("comments", JoinType.LEFT);
		q = q.multiselect(r.get("id").alias("id"), b.count(join.get("article").get("id")).alias("count"));
		if (articleIds instanceof Collection) {
			q = q.where(r.get("id").in(articleIds));
		}
		q = q.groupBy(r.get("id"), join.get("article").get("id"));
		return entityManager.createQuery(q).getResultList().stream().collect(
				Collectors.toMap(tuple -> tuple.get("id", Long.class), tuple -> tuple.get("count", Long.class)));
	}
	
}
