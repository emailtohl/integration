package com.github.emailtohl.integration.core.user;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchableRepository;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 用户管理数据访问的自定义接口
 * @author HeLei
 */
interface UserRepositoryCustomization extends SearchableRepository<User> {
	
}
