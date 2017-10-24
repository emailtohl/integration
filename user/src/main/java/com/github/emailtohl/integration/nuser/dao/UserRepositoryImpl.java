package com.github.emailtohl.integration.nuser.dao;

import javax.persistence.AccessType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.jpaCriterionQuery.AbstractCriterionQueryRepository;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * spring data扫描到接口UserRepository时，会认为UserRepository+Impl作为自定义实现
 * 当调用UserRepositoryImpl中的方法时，不再代理，而是直接将方法交给UserRepositoryImpl
 * @author HeLei
 * @date 2017.02.04
 */
//@Repository //不由spring管理，而是由spring data管理
public class UserRepositoryImpl extends AbstractCriterionQueryRepository<User> implements UserRepositoryCustomization {

	/**
	 * 使用spring data使用的page对象，暂不支持Pageable中的排序功能
	 * 默认使用JavaBean属性获取查询条件
	 */
	@Override
	public Page<User> getPage(User user, Pageable pageable) {
		Paging<User> myPage = getPage(user, pageable.getPageNumber(), pageable.getPageSize(), AccessType.PROPERTY);
		Page<User> springPage = new PageImpl<User>(myPage.getContent(), pageable, myPage.getTotalElements());
		return springPage;
	}

}
