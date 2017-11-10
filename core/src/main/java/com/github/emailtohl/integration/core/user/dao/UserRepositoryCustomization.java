package com.github.emailtohl.integration.core.user.dao;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 用户管理数据访问的自定义接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface UserRepositoryCustomization extends SearchableRepository<User> {
	
}
