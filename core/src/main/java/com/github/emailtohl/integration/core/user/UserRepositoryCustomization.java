package com.github.emailtohl.integration.core.user;

import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.lib.jpa.SearchInterface;

/**
 * 用户管理数据访问的自定义接口
 * @author HeLei
 */
interface UserRepositoryCustomization extends SearchInterface<User, Long> {
	
}
