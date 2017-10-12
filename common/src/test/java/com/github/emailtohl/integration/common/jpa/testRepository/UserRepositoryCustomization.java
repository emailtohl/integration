package com.github.emailtohl.integration.common.jpa.testRepository;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa._Page;
import com.github.emailtohl.integration.common.jpa.jpaCriterionQuery.CriterionQueryRepository;
import com.github.emailtohl.integration.common.testEntities.User;
/**
 * 用户管理数据访问的自定义接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface UserRepositoryCustomization extends CriterionQueryRepository<User> {
	
	_Page<User> dynamicQuery(User user, Pageable pageable);
	
	/**
	 * 根据用户授权来查询Page，由于User与Authority是多对多关系，自定义的动态查询不满足需求，所以新开辟一个接口
	 * @param email
	 * @param roles
	 * @param pageable
	 * @return
	 */
	_Page<User> getPageByRoles(String email, Set<String> roleNames, Pageable pageable);
	
	/**
	 * 通过JPA2.1的标准查询（Criteria）方式获取Page，支持排序
	 * @param email
	 * @param roles
	 * @param pageable
	 * @return
	 */
	_Page<User> getPageByCriteria(String email, Set<String> roleNames, Pageable pageable);
	
	/**
	 * 添加Spring data的分页功能，暂不支持Pageable中的排序功能
	 * 默认使用JavaBean属性获取查询条件
	 */
	Page<User> getPage(User entity, Pageable pageable);
	
	/**
	 * 获取最大的emp_no
	 */
	Integer getMaxEmpNo();
	
	/**
	 * 将缓存刷新到数据库
	 */
	void flush();
}
