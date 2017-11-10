package com.github.emailtohl.integration.core.user.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;

public interface CustomerRepositoryCustomization extends SearchableRepository<Customer> {
	Page<Customer> queryForPage(Customer params, Pageable pageable);
}
