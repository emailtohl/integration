package com.github.emailtohl.integration.nuser.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.nuser.entities.Customer;

/**
 * 只查询客户的数据访问接口
 * 
 * @author HeLei
 * @date 2017.02.04
 */
public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustomization {
	
	Customer findByCellPhone(String cellPhone);

	Customer findByEmail(String email);
}
