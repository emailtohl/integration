package com.github.emailtohl.integration.core.user;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * spring data扫描到接口UserRepository时，会认为UserRepository+Impl作为自定义实现
 * 当调用UserRepositoryImpl中的方法时，不再代理，而是直接将方法交给UserRepositoryImpl
 * @author HeLei
 */
//@Repository //不由spring管理，而是由spring data管理
class UserRepositoryImpl extends AbstractSearchableRepository<User> implements UserRepositoryCustomization {

}
