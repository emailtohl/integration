package com.github.emailtohl.integration.web.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
/**
 * 在spring security过滤器之后执行
 * Servlet Filter implementation class PostSecurityLoggingFilter
 * @author HeLei
 * @date 2017.02.04
 */
//@WebFilter("/*")
public class PostSecurityLoggingFilter implements Filter {
	public static final Logger logger = LogManager.getLogger(PostSecurityLoggingFilter.class);
	/**
	 * Default constructor.
	 */
	public PostSecurityLoggingFilter() {
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			Authentication authentication = context.getAuthentication();
			if (authentication != null) {
				logger.debug("username: " + authentication.getName());
				logger.debug("Credentials: " + authentication.getCredentials());
				logger.debug("Details: " + authentication.getDetails());
				Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
				int i = 1;
				for (GrantedAuthority g : grantedAuthorities) {
					logger.debug("authority " + i + ": " + g.getAuthority());
					i++;
				}
				Object principal = authentication.getPrincipal();
				if (principal instanceof User) {
					User u = (User) principal;
					logger.debug("username: " + u.getUsername());
				} else {
					logger.debug("Principal: " + principal);
				}
			}
			request.setAttribute("authentication", authentication);
		}
		logger.debug("\n");
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {}

}
