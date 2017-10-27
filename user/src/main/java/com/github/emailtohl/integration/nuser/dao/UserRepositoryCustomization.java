package com.github.emailtohl.integration.nuser.dao;

import com.github.emailtohl.integration.common.jpa.jpaCriterionQuery.CriterionQueryRepository;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * 用户管理数据访问的自定义接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface UserRepositoryCustomization extends CriterionQueryRepository<User> {
	
}
