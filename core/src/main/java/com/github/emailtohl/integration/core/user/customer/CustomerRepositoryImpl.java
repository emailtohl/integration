package com.github.emailtohl.integration.core.user.customer;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;

/**
 * 客户管理数据访问接口实现类
 * @author HeLei
 */
@Repository
class CustomerRepositoryImpl extends AbstractSearchableRepository<Customer> implements CustomerRepositoryCustomization {

	@Override
	public Customer create(Customer customer) {
		entityManager.persist(customer);
		CustomerRef ref = new CustomerRef(customer);
		customer.setCustomerRef(ref);
		entityManager.persist(ref);
		return customer;
	}

	@Override
	public Customer findByUsername(String username) {
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<Customer> q = b.createQuery(entityClass);
		Root<Customer> r = q.from(entityClass);
		q = q.select(r).where(b.equal(r.join("usernames"), username));
		Customer result = null;
		try {
			result = entityManager.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		return result;
	}

	@Override
	public CustomerRef findRefByUsername(String username) {
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<CustomerRef> q = b.createQuery(CustomerRef.class);
		Root<Customer> r = q.from(entityClass);
		q = q.select(r.get("customerRef")).where(b.equal(r.join("usernames"), username));
		CustomerRef result = null;
		try {
			result = entityManager.createQuery(q).getSingleResult();
		} catch (NoResultException e) {}
		return result;
	}

	@Override
	public List<String> getUsernames(Long id) {
		CriteriaBuilder b = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> q = b.createQuery(String.class);
		Root<Customer> r = q.from(entityClass);
		q = q.select(r.join("usernames")).where(b.equal(r.get("id"), id));
		return entityManager.createQuery(q).getResultList();
	}

}
