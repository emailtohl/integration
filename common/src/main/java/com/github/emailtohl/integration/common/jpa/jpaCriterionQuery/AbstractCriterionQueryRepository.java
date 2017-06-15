package com.github.emailtohl.integration.common.jpa.jpaCriterionQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

import com.github.emailtohl.integration.common.jpa.AbstractDynamicQueryRepository;

/**
 * 提供标准查询的基类
 * 
 * 注意：调用者需根据业务情况明确事务边界，添加上@javax.transaction.Transactional
 * 
 * @param <E> 实体类
 * @author HeLei
 * @date 2017.02.04
 */
public abstract class AbstractCriterionQueryRepository<E extends Serializable> extends AbstractDynamicQueryRepository<E>
		implements CriterionQueryRepository<E> {
	/**
	 * 标准查询接口，根据传入的条件集合得到一个Page对象 注意:Pageable的查询是从第0页开始，条件集合之间是AND关系
	 * 
	 * @param criteria 一个条件集合
	 * @param pageable 分页对象
	 * @return
	 */
	@Override
	public Page<E> search(Collection<Criterion> criteria, Pageable pageable) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		Root<E> countRoot = countQuery.from(entityClass);
		long total = entityManager
				.createQuery(
						countQuery.select(builder.count(countRoot)).where(toPredicates(criteria, countRoot, builder)))
				.getSingleResult();

		CriteriaQuery<E> query = builder.createQuery(entityClass);
		Root<E> queryRoot = query.from(entityClass);
		List<E> list = entityManager
				.createQuery(query.select(queryRoot).where(toPredicates(criteria, queryRoot, builder))
						.orderBy(QueryUtils.toOrders(pageable.getSort(), queryRoot, builder)))
				.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();

		return new PageImpl<E>(new ArrayList<E>(list), pageable, total);
	}

	private Predicate[] toPredicates(Collection<Criterion> criteria, Root<?> root, CriteriaBuilder builder) {
		Predicate[] predicates = new Predicate[criteria.size()];
		int i = 0;
		for (Criterion c : criteria)
			predicates[i++] = c.getOperator().toPredicate(c, root, builder);
		return predicates;
	}

	public AbstractCriterionQueryRepository() {
		super();
	}

	public AbstractCriterionQueryRepository(Class<E> entityClass) {
		super(entityClass);
	}
	
}
