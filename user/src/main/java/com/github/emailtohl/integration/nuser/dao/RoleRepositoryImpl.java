package com.github.emailtohl.integration.nuser.dao;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;
import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.jpaCriterionQuery.AbstractCriterionQueryRepository;
import com.github.emailtohl.integration.nuser.entities.Role;

/**
 * 自定义接口的实现
 * 
 * @author HeLei
 */
public class RoleRepositoryImpl extends AbstractCriterionQueryRepository<Role> implements RoleRepositoryCustomization {
	@PersistenceContext
	EntityManager em;

	@Override
	public Page<Role> query(String roleName, String authorityName, Pageable pageable) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Role> q = cb.createQuery(Role.class);
		Root<Role> r = q.from(Role.class);

		// 返回结果为User的谓词
		Predicate[] predicates = getPredicates(roleName, authorityName, cb, r);

		q = q.distinct(true).select(r).where(predicates).orderBy(toOrders(pageable.getSort(), r, cb));
		List<Role> contents = em.createQuery(q).setFirstResult(pageable.getOffset())
				.setMaxResults(pageable.getPageSize()).getResultList();

		// 查询总条数
		CriteriaQuery<Long> qc = cb.createQuery(Long.class);
		r = qc.from(Role.class);
		// 返回结果为Long的谓词
		predicates = getPredicates(roleName, authorityName, cb, r);
		qc = qc.distinct(true).select(cb.count(r)).where(predicates);
		long total = em.createQuery(qc).getSingleResult();

		return new PageImpl<Role>(contents, pageable, total);
	}

	@Override
	public List<Role> getRoleList(String roleName, String authorityName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Role> q = cb.createQuery(Role.class);
		Root<Role> r = q.from(Role.class);

		// 返回结果为User的谓词
		Predicate[] predicates = getPredicates(roleName, authorityName, cb, r);

		q = q.distinct(true).select(r).where(predicates);
		return em.createQuery(q).getResultList();
	}

	/**
	 * 根据User的值域获取查询的谓词
	 * 
	 * @param params
	 *            查询参数
	 * @param cb
	 *            CriteriaQuery的构造器
	 * @param r
	 *            查询的root
	 * @return 谓词集合
	 */
	private Predicate[] getPredicates(String roleName, String authorityName, CriteriaBuilder cb, Root<Role> r) {
		// 谓词集合
		List<Predicate> ls = new ArrayList<Predicate>();
		if (hasText(roleName)) {
			ls.add(cb.like(r.<String>get("name"), "%" + roleName.trim() + "%"));
		}
		if (hasText(authorityName)) {
			ls.add(r.join("authorities").<String>get("name").in(authorityName));
		}
		return ls.toArray(new Predicate[ls.size()]);
	}

	@Override
	public Set<Long> roleIdsWhichCanNotBeDeleted() {
		List<?> ls = em.createNativeQuery("SELECT role_id FROM t_user_role").getResultList();
		Set<Long> set = new HashSet<Long>();
		for (Object o : ls) {
			set.add(Long.parseLong(o.toString()));
		}
		return set;
	}

	@Override
	public boolean exist(String roleName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> q = cb.createQuery(Long.class);
		Root<Role> r = q.from(Role.class);
		q = q.select(cb.count(r)).where(cb.equal(r.get("name"), roleName));
		Long count = em.createQuery(q).getSingleResult();
		return count > 0;
	}
}
