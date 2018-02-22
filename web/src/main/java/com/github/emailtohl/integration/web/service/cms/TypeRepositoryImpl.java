package com.github.emailtohl.integration.web.service.cms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.util.StringUtils;

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
	public List<Type> getTypesWithArticleNum(Type params) {
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> q = b.createTupleQuery();
		Root<Type> r = q.from(Type.class);
		Join<Type, Article> join = r.join("articles", JoinType.LEFT);
		q = q.multiselect(r.alias("type"), b.count(join.get("type").get("id")).alias("count"));
		List<Predicate> ls = new ArrayList<Predicate>();
		if (params != null) {
			if (params.getId() != null) {
				ls.add(b.equal(r.get("id"), params.getId()));
			}
			if (StringUtils.hasText(params.getName())) {
				ls.add(b.like(b.lower(r.get("name")), params.getName().trim().toLowerCase()));
			}
			if (StringUtils.hasText(params.getDescription())) {
				ls.add(b.like(b.lower(r.get("description")), params.getDescription().trim().toLowerCase()));
			}
			if (!ls.isEmpty()) {
				Predicate[] predicates = new Predicate[ls.size()];
				q = q.where(ls.toArray(predicates));
			}
		}
		q = q.groupBy(r.get("id"), join.get("type").get("id"));
		return entityManager.createQuery(q).getResultList().stream().map(tuple -> {
			Type t = tuple.get("type", Type.class);
			t.setArticlesNum(tuple.get("count", Long.class).intValue());
			return t;
		}).collect(Collectors.toList());
	}

}
