package com.github.emailtohl.integration.nuser.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.nuser.entities.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, EmployeeRepositoryCustomization {
	
	Employee findByEmpNum(Integer empNum);
	
}
