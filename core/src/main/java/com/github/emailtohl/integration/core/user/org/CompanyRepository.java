package com.github.emailtohl.integration.core.user.org;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.Company;

/**
 * 公司数据层
 * @author HeLei
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {
	
	Company findByName(String name);
	
	List<Company> findByParentId(Long id);
	
}
