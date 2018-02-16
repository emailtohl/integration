package com.github.emailtohl.integration.web.service.cms;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import com.github.emailtohl.integration.web.service.cms.entities.Article;
import com.github.emailtohl.integration.web.service.cms.entities.Type;
/**
 * 类型自定义数据层
 * @author HeLei
 */
public class TypeRepositoryImpl implements TypeRepositoryCustomization {
	@PersistenceContext
	EntityManager entityManager;
	/*
	SELECT
		T . *,
		COUNT (A .article_type_id)
	FROM
		article_type T
	LEFT JOIN article A ON T . ID = A .article_type_id
	GROUP BY
		T . ID,
		A .article_type_id
	*/
	@Override
	public List<Type> getTypesWithArticleNum(Type type) {
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> q = b.createQuery(Object[].class);
		Root<Type> r = q.from(Type.class);
		Join<Type, Article> join = r.join("articles");
		q = q.multiselect(r, b.count(join.get("type").get("id")));
		q = q.groupBy(r.get("id"), join.get("type").get("id"));
		return entityManager.createQuery(q).getResultList().stream().map(tuple -> {
			Type t = (Type) tuple[0];
			t.setArticlesNum((Integer) tuple[1]);
			return t;
		}).collect(Collectors.toList());
	}

}
