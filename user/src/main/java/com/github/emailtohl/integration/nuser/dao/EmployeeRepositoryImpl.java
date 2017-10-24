package com.github.emailtohl.integration.nuser.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.nuser.entities.Employee;

public class EmployeeRepositoryImpl extends AbstractSearchableRepository<Employee> implements EmployeeRepositoryCustomization {
	public Page<Employee> queryForPage(Employee params, Pageable pageable) {
		return super.queryForPage(params, pageable);
	}
}
