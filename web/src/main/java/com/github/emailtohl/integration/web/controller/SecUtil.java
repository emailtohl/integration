package com.github.emailtohl.integration.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

/**
 * 获取Spring Security上下文的当前用户
 * @author HeLei
 * @date 2017.06.19
 */
public final class SecUtil {
	private SecUtil() {}
	
	public static String getCurrentUsername() {
		String username = null;
		SecurityContext ctx = SecurityContextHolder.getContext();
		if (ctx != null) {
			Authentication a = ctx.getAuthentication();
			if (a != null) {
				username = a.getName();
			}
		}
		if (!StringUtils.hasText(username)) {
			throw new UsernameNotFoundException("没有此用户:" + username);
		}
		return username;
	}
}
