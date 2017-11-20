package com.github.emailtohl.integration.core.user.employee;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.core.user.entities.Employee;

interface EmployeeRepositoryCustomization extends SearchableRepository<Employee> {
	/**
	 * 获取最大的emp_no
	 */
	Integer getMaxEmpNo();
}
