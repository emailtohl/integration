package com.github.emailtohl.integration.common.jpa.testRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.common.testEntities.Customer;
/**
 * 只查询客户的数据访问接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface CustomerRepository extends SearchableRepository<Customer> {
	Page<Customer> query(String name, String title, String affiliation, Pageable pageable);

	Customer getCustomer(Long id);

	void merge(Customer c);

	void delete(Long id);

	List<Customer> findAll();
}
