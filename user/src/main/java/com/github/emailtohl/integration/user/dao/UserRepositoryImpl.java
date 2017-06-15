package com.github.emailtohl.integration.user.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AccessType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

import com.github.emailtohl.integration.common.jpa.Pager;
import com.github.emailtohl.integration.common.jpa.jpaCriterionQuery.AbstractCriterionQueryRepository;
import com.github.emailtohl.integration.user.entities.Employee;
import com.github.emailtohl.integration.user.entities.User;
/**
 * spring data扫描到接口UserRepository时，会认为UserRepository+Impl作为自定义实现
 * 当调用UserRepositoryImpl中的方法时，不再代理，而是直接将方法交给UserRepositoryImpl
 * @author HeLei
 * @date 2017.02.04
 */
//@Repository //不由spring管理，而是由spring data管理
public class UserRepositoryImpl extends AbstractCriterionQueryRepository<User> implements UserRepositoryCustomization {

	@Override
	public Pager<User> dynamicQuery(User user, Pageable pageable) {
		return super.getPager(user, pageable.getPageNumber(), pageable.getPageSize(), AccessType.PROPERTY);
	}
	
	@Override
	public Pager<User> getPagerByRoles(String email, Set<String> roleNames, Pageable pageable) {
		StringBuilder jpql = new StringBuilder("SELECT DISTINCT u FROM User u WHERE 1 = 1");
		Map<String, Object> args = new HashMap<String, Object>();
		if (roleNames != null && roleNames.size() > 0) {
			// 仅当有一对多关系存在时再插入JOIN语句，否则底层SQL语句就只能查找存在外联关系的数据了
			int i = jpql.indexOf("WHERE");
			jpql.insert(i, "JOIN u.roles r ").append(" AND r.name IN :roleNames");
			args.put("roleNames", roleNames);
		}
		if (email != null && email.length() > 0) {
			jpql.append(" AND u.email LIKE :email");
			args.put("email", email);
		}
		return super.getPager(jpql.toString(), args, pageable.getPageNumber(), pageable.getPageSize());
	}
	
	@Override
	public Pager<User> getPagerByCriteria(String email, Set<String> roleNames, Pageable pageable) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<Long> q1 = cb.createQuery(Long.class);
		Root<User> r1 = q1.from(entityClass);
		
		List<Predicate> lp = new ArrayList<Predicate>();
		if (email != null && !email.isEmpty()) {
			lp.add(cb.like(r1.get("email"), email));
		}
		if (roleNames != null && !roleNames.isEmpty()) {
			lp.add(r1.join("roles").get("name").in(roleNames));
		}
		Predicate[] ps = lp.toArray(new Predicate[lp.size()]);
		
		q1.distinct(true).select(cb.count(r1)).where(ps);
		
		Long count = entityManager.createQuery(q1).getSingleResult();

		CriteriaQuery<User> q2 = cb.createQuery(entityClass);
		Root<User> r2 = q2.from(entityClass);
		
		lp.clear();
		if (email != null && !email.isEmpty()) {
			lp.add(cb.like(r2.get("email"), email));
		}
		if (roleNames != null && !roleNames.isEmpty()) {
			lp.add(r2.join("roles").get("name").in(roleNames));
		}
		ps = lp.toArray(new Predicate[lp.size()]);
		
		q2.distinct(true).select(r2).where(ps)
		.orderBy(QueryUtils.toOrders(pageable.getSort(), r2, cb));

		List<User> ls = entityManager.createQuery(q2)
				.setFirstResult(pageable.getOffset())
				.setMaxResults(pageable.getPageSize())
				.getResultList();
		
		return new Pager<User>(ls, count, pageable.getPageNumber(), pageable.getPageSize());
	}

	/**
	 * 使用spring data使用的page对象，暂不支持Pageable中的排序功能
	 * 默认使用JavaBean属性获取查询条件
	 */
	@Override
	public Page<User> getPage(User user, Pageable pageable) {
		Pager<User> myPager = getPager(user, pageable.getPageNumber(), pageable.getPageSize(), AccessType.PROPERTY);
		Page<User> springPage = new PageImpl<User>(myPager.getContent(), pageable, myPager.getTotalElements());
		return springPage;
	}

	@Override
	public Integer getMaxEmpNo() {
		Integer result;
//		result = entityManager.createQuery("select max(e.empNum) from Employee e", Integer.class).getSingleResult();
		
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<Integer> q = b.createQuery(Integer.class);
		Root<Employee> r = q.from(Employee.class);
		result = entityManager.createQuery(q.select(b.max(r.get("empNum")))).getSingleResult();
			
		return result;
	}

	@Override
	public void flush() {
		entityManager.flush();
	}
	
}
