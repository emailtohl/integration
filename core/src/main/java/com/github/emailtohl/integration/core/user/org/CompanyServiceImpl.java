package com.github.emailtohl.integration.core.user.org;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.user.entities.Company;

/**
 * 公司服务层实现
 * @author HeLei
 */
@Transactional
public class CompanyServiceImpl implements CompanyService {
	@Inject
	CompanyRepository companyRepository;

	@Override
	public Company create(Company entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exist(Object matcherValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Company get(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Paging<Company> query(Company params, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Company> query(Company params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Company update(Long id, Company newEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Long id) {
		// TODO Auto-generated method stub
		
	}

}
