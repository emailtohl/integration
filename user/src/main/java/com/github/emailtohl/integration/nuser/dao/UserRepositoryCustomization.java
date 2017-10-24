package com.github.emailtohl.integration.nuser.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.jpaCriterionQuery.CriterionQueryRepository;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * 用户管理数据访问的自定义接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface UserRepositoryCustomization extends CriterionQueryRepository<User> {
	
	/**
	 * 默认使用JavaBean属性获取查询条件
	 */
	Page<User> getPage(User entity, Pageable pageable);
	
}
