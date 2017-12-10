package com.github.emailtohl.integration.core.auth;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.entities.User;
/**
 * 本类实现了UserDetailsService，用于spring security配置AuthenticationManagerBuilder中
 * @author HeLei
 */
@Service
@Named("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
	@Inject
	UserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User u = userService.find(username);
		if (u == null) {
			throw new UsernameNotFoundException("没有此用户：" + username);
		}
		UserDetails d = new UserDetailsImpl(u, username/*用查找的用户名*/);
		return d;
	}

}
