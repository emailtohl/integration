package com.github.emailtohl.integration.core.user.customer;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;

/**
 * 自定义
 * @author HeLei
 */
interface CustomerRepositoryCustomization extends SearchableRepository<Customer> {
	
	Customer create(Customer customer);
	
	Customer findByUsername(String username);
	
	CustomerRef findRefByUsername(String username);
}
