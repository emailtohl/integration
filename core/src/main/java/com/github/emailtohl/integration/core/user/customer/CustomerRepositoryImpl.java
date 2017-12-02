package com.github.emailtohl.integration.core.user.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	public Page<Customer> queryForPage(Customer params, Pageable pageable) {
		return super.queryForPage(params, pageable);
	}

}
