package com.github.emailtohl.integration.core.user.employee;

import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.lib.jpa.SearchInterface;

interface EmployeeRepositoryCustomization extends SearchInterface<Employee, Long> {
	
	/**
	 * 创建一个平台账户实例，同时包括其引用
	 * 
	 * @param employee
	 * @return
	 */
	Employee create(Employee employee);
	
	/**
	 * 获取最大的emp_no
	 */
	Integer getMaxEmpNo();
}
