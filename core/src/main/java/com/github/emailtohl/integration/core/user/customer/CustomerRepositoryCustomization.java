package com.github.emailtohl.integration.core.user.customer;

import java.util.List;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;

/**
 * 自定义
 * @author HeLei
 */
interface CustomerRepositoryCustomization extends SearchableRepository<Customer> {
	
	/**
	 * 创建一个客户实例，同时包括其引用
	 * 
	 * @param customer
	 * @return
	 */
	Customer create(Customer customer);
	
	/**
	 * 查询该客户唯一标识是否已用
	 * 
	 * @param username 客户唯一标识，如邮箱、手机号等
	 * @return
	 */
	boolean usernameIsExist(String username);
	
	/**
	 * 根据客户唯一标识查询客户实体对象
	 * 
	 * @param username 客户唯一标识，如邮箱、手机号等
	 * @return
	 */
	Customer findByUsername(String username);
	
	/**
	 * 根据客户唯一标识查询客户实体的引用
	 * 
	 * @param username 客户唯一标识，如邮箱、手机号等
	 * @return
	 */
	CustomerRef findRefByUsername(String username);
	
	/**
	 * 通过id查找所有的客户唯一标识，如邮箱、手机号等
	 * @param id
	 * @return username 客户唯一标识，如邮箱、手机号等
	 */
	List<String> getUsernames(Long id);
}
