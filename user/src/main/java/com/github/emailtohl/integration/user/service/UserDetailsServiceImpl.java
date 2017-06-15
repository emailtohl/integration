package com.github.emailtohl.integration.user.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.Constant;
import com.github.emailtohl.integration.common.exception.ResourceNotFoundException;
import com.github.emailtohl.integration.user.entities.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Inject
	UserService userService;
	
	private transient Pattern p = Pattern.compile(Constant.PATTERN_EMAIL);
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Matcher m = p.matcher(username);
		if (!m.find()) {
			throw new UsernameNotFoundException("请使用正确邮箱");
		}
		String email = m.group(1);
		User u;
		try {
			u = userService.getUserByEmail(email);
		} catch (ResourceNotFoundException e) {
			throw new UsernameNotFoundException("没有此用户");
		}
		return u.getUserDetails();
	}

}
