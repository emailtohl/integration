package com.github.emailtohl.integration.core.user.employee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.Employee;

/**
 * 平台账号数据访问层
 * @author HeLei
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long>, EmployeeRepositoryCustomization {
	
	Employee findByCellPhone(String cellPhone);

	Employee findByEmail(String email);
	
	Employee findByEmpNum(Integer empNum);
	
	List<Employee> findByNameLike(String name);
}
