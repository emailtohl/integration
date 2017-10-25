package com.github.emailtohl.integration.nuser.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.nuser.entities.Customer;

public interface CustomerRepositoryCustomization extends SearchableRepository<Customer> {
	Page<Customer> queryForPage(Customer params, Pageable pageable);
}
