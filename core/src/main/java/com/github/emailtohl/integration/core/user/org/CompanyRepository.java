package com.github.emailtohl.integration.core.user.org;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.Company;

/**
 * 公司数据层
 * @author HeLei
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {
	
	/**
	 * 根据公司名查询公司
	 * @param name
	 * @return
	 */
	Company findByName(String name);
	
	/**
	 * 查询父id下的所有公司
	 * @param id
	 * @return
	 */
	List<Company> findByParentId(Long id);
	
}
