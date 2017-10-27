package com.github.emailtohl.integration.nuser.service;

import javax.inject.Inject;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.nuser.dao.UserRepository;
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * 统一查询功能
 * @author HeLei
 */
@Service
public class UserServiceImpl implements UserService {
	@Inject
	UserRepository userRepository;
	
	@Override
	public Page<User> query(User params, Pageable pageable) {
		Page<User> p = userRepository.queryForPage(params, pageable);
		
		return null;
	}

	private User filter(User source) {
		User target = new User();
		BeanUtils.copyProperties(source, target, Customer.getIgnoreProperties("password"));
		target.setId(source.getId());
		return target;
	}
}
