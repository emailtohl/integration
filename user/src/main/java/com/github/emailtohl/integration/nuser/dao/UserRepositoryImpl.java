package com.github.emailtohl.integration.nuser.dao;

import com.github.emailtohl.integration.common.jpa.fullTextSearch.AbstractSearchableRepository;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * spring data扫描到接口UserRepository时，会认为UserRepository+Impl作为自定义实现
 * 当调用UserRepositoryImpl中的方法时，不再代理，而是直接将方法交给UserRepositoryImpl
 * @author HeLei
 * @date 2017.02.04
 */
//@Repository //不由spring管理，而是由spring data管理
public class UserRepositoryImpl extends AbstractSearchableRepository<User> implements UserRepositoryCustomization {

}
