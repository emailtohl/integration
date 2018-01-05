package com.github.emailtohl.integration.core.user.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.Customer;

/**
 * 只查询客户的数据访问接口
 * 
 * @author HeLei
 */
public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustomization {
	
	Customer findByCellPhone(String cellPhone);

	Customer findByEmail(String email);
	
}
