package com.github.emailtohl.integration.nuser.service;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * 统一查询功能
 * @author HeLei
 */
@Transactional
@Validated
public interface UserService {
	/**
	 * 全文搜索
	 * @param fulltext
	 * @param pageable
	 * @return
	 */
	Paging<User> search(String fulltext, Pageable pageable);
	
	/**
	 * 根据域条件查询
	 * @param params
	 * @param pageable
	 * @return
	 */
	Paging<User> query(User params, Pageable pageable);
	
	/**
	 * 查看该User
	 * @param id
	 * @return
	 */
	User get(Long id);
	
}
