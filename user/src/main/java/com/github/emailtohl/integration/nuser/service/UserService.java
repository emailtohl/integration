package com.github.emailtohl.integration.nuser.service;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.nuser.entities.User;

/**
 * 统一查询功能
 * @author HeLei
 */
@Transactional
@Validated
public interface UserService {
	
	Page<User> query(User params, Pageable pageable);
	
}
