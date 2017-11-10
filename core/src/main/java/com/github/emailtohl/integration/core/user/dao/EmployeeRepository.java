package com.github.emailtohl.integration.core.user.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, EmployeeRepositoryCustomization {
	
	Employee findByEmpNum(Integer empNum);
	
	List<Employee> findByNameLike(String name);
}
