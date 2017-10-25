package com.github.emailtohl.integration.nuser.dao;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.nuser.entities.Employee;

public interface EmployeeRepositoryCustomization extends SearchableRepository<Employee> {
	/**
	 * 获取最大的emp_no
	 */
	Integer getMaxEmpNo();
}
