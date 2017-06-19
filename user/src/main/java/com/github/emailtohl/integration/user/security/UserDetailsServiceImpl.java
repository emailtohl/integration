package com.github.emailtohl.integration.user.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.Constant;
import com.github.emailtohl.integration.user.dao.UserRepository;
import com.github.emailtohl.integration.user.entities.User;
/**
 * 本类实现了UserDetailsService
 * @author HeLei
 * @date 2017.06.15
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private UserRepository userRepository;
	
	@Inject
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	private transient Pattern p = Pattern.compile(Constant.PATTERN_EMAIL);
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Matcher m = p.matcher(username);
		if (!m.find()) {
			throw new UsernameNotFoundException("请使用正确邮箱");
		}
		String email = m.group(1);
		User u = userRepository.findByEmail(email);
		if (u == null) {
			throw new UsernameNotFoundException("没有此用户");
		}
		UserDetails d = new UserDetailsImpl(u);
		return d;
	}

}
