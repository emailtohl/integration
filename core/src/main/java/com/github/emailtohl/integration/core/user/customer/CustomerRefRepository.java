package com.github.emailtohl.integration.core.user.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.CustomerRef;

/**
 * 查询顾客引用
 * @author HeLei
 */
interface CustomerRefRepository extends JpaRepository<CustomerRef, Long> {

	Page<CustomerRef> findByCellPhone(String cellPhone, Pageable pageable);
	
	CustomerRef findByCellPhone(String cellPhone);
	
	Page<CustomerRef> findByEmail(String email, Pageable pageable);
	
	CustomerRef findByEmail(String email);
	
}
