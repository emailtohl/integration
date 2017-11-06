package com.github.emailtohl.integration.nuser.auth;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.nuser.entities.User;
/**
 * 本类实现了UserDetailsService，用于spring security配置AuthenticationManagerBuilder中
 * @author HeLei
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Inject
	LoadUser loadUser;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User u = loadUser.load(username);
		if (u == null) {
			throw new UsernameNotFoundException("没有此用户：" + username);
		}
		UserDetails d = new UserDetailsImpl(u);
		return d;
	}

}
