package com.github.emailtohl.integration.core.user.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.EmployeeRef;

/**
 * 查询平台账号的引用
 * @author HeLei
 */
public interface EmployeeRefRepository extends JpaRepository<EmployeeRef, Long> {

	Page<EmployeeRef> findByCellPhone(String cellPhone, Pageable pageable);

	EmployeeRef findByCellPhone(String cellPhone);

	Page<EmployeeRef> findByEmail(String email, Pageable pageable);

	EmployeeRef findByEmail(String email);
	
	EmployeeRef findByEmpNum(Integer empNum);

}
