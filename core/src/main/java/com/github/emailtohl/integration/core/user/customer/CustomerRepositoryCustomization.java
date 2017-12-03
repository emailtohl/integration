package com.github.emailtohl.integration.core.user.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;

/**
 * 自定义
 * @author HeLei
 */
interface CustomerRepositoryCustomization extends SearchableRepository<Customer> {
	
	Customer create(Customer customer);
	
	Page<Customer> queryForPage(Customer params, Pageable pageable);
}
