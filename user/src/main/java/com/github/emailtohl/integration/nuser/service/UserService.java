package com.github.emailtohl.integration.nuser.service;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.nuser.entities.User;

@Transactional
@Validated
public interface UserService {
	
	Page<User> queryForPage(User params, Pageable pageable);
	
}
