package com.github.emailtohl.integration.nuser.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.nuser.entities.Customer;

/**
 * 客户管理数据访问接口实现类
 * @author HeLei
 * @date 2017.02.04
 */
@Repository
public class CustomerRepositoryImpl extends AbstractSearchableRepository<Customer> implements CustomerRepositoryCustomization {

	@Override
	public Page<Customer> queryForPage(Customer params, Pageable pageable) {
		return super.queryForPage(params, pageable);
	}

}
