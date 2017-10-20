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
 * @param <E>
 *            实体类
 * @author HeLei
 * @date 2017.02.04
 */
public abstract class AbstractCriterionQueryRepository<E extends Serializable> extends AbstractDynamicQueryRepository<E>
		implements CriterionQueryRepository<E> {
	/**
	 * 标准查询接口，根据传入的条件集合得到一个Page对象
	 * 注意:Pageable的查询是从第0页开始，条件集合之间是AND关系
	 * 
	 * @param criteria
	 *            一个条件集合
	 * @param pageable
	 *            分页对象
	 * @return
	 */
	@Override
	public Page<E> query(Collection<Criterion> criteria, Pageable pageable) {
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

	/**
	 * 标准查询接口，根据传入的条件集合得到一个Page对象
	 * 注意，Pageable的查询是从第0页开始，条件集合之间是AND关系
	 * 
	 * @param criteria 一个条件集合
	 * @return
	 */
	@Override
	public List<E> query(Collection<Criterion> criteria) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(entityClass);
		Root<E> queryRoot = query.from(entityClass);
		return entityManager.createQuery(query.select(queryRoot).where(toPredicates(criteria, queryRoot, builder)))
				.getResultList();

	}

	protected Predicate[] toPredicates(Collection<Criterion> criteria, Root<?> root, CriteriaBuilder builder) {
		Predicate[] predicates = new Predicate[criteria.size()];
		int i = 0;
		for (Criterion c : criteria)
			predicates[i++] = c.getOperator().toPredicate(c, root, builder);
		return predicates;
	}
	
	/**
	 * 将对象存储的值转成谓词集合
	 * 
	 * 注意：1.不包括对象中集合属性； 2.谓词之间均为AND连接；3.浮点数也是用相等比较导致查询不到结果
	 * @param e 实体参数
	 * @param type 分析对象的方式
	 * @return
	 */
/*	protected Collection<Criterion> getCriteriaExcludeCollection(E e, AccessType type) {
		Set<Criterion> criteria = new HashSet<Criterion>();
		handle(criteria, "", e, type);
		return criteria;
	}
	
	private void handle(Collection<Criterion> criteria, String path, Object e, AccessType type) {
		if (type == null || type == AccessType.PROPERTY) {
			for (Entry<String, Object> entry : BeanUtil.getPropertyNameValueMap(e).entrySet()) {
				Object value = entry.getValue();
				if (value == null || value instanceof Collection) {
					continue;
				}
				if (value instanceof Number || value instanceof Boolean) {
					String _path = StringUtils.hasText(path) ? path + '.' + entry.getKey() : entry.getKey();
					criteria.add(new Criterion(_path, Operator.EQ, value));
				} else if (value instanceof String) {
					String _path = StringUtils.hasText(path) ? path + '.' + entry.getKey() : entry.getKey();
					criteria.add(new Criterion(_path, Operator.LIKE, ((String) value).trim()));
				} else {
					String _path = StringUtils.hasText(path) ? path + '.' + entry.getKey() : entry.getKey();
					handle(criteria, _path, value, type);
				}
			}
		} else {
			for (Entry<String, Object> entry : BeanUtil.getFieldNameValueMap(e).entrySet()) {
				Object value = entry.getValue();
				if (value == null || value instanceof Collection) {
					continue;
				}
				if (value instanceof Number || value instanceof Boolean) {
					String _path = StringUtils.hasText(path) ? path + '.' + entry.getKey() : entry.getKey();
					criteria.add(new Criterion(_path, Operator.EQ, value));
				} else if (value instanceof String) {
					String _path = StringUtils.hasText(path) ? path + '.' + entry.getKey() : entry.getKey();
					criteria.add(new Criterion(_path, Operator.LIKE, ((String) value).trim()));
				} else {
					String _path = StringUtils.hasText(path) ? path + '.' + entry.getKey() : entry.getKey();
					handle(criteria, _path, value, type);
				}
			}
		}
	}*/

	public AbstractCriterionQueryRepository() {
		super();
	}

	public AbstractCriterionQueryRepository(Class<E> entityClass) {
		super(entityClass);
	}

}
