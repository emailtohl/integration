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
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.github.emailtohl.integration.core.config.Constant;
/**
 * 在spring security过滤器之后执行
 * Servlet Filter implementation class PostSecurityLoggingFilter
 * @author HeLei
 */
//@WebFilter("/*")
public class PostSecurityLoggingFilter implements Filter {
	public static final Logger LOG = LogManager.getLogger();
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
		String username = Constant.ANONYMOUS_NAME;
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			Authentication authentication = context.getAuthentication();
			if (authentication != null) {
				username = authentication.getName();
				if (LOG.isDebugEnabled()) {
					LOG.debug("username: " + authentication.getName());
					LOG.debug("Credentials: " + authentication.getCredentials());
					LOG.debug("Details: " + authentication.getDetails());
					Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
					if (grantedAuthorities != null) {
						int i = 1;
						for (GrantedAuthority g : grantedAuthorities) {
							LOG.debug("authority " + i + ": " + g.getAuthority());
							i++;
						}
					}
				}
				Object principal = authentication.getPrincipal();
				if (principal instanceof User) {
					User u = (User) principal;
					if (LOG.isDebugEnabled())
						LOG.debug("username: " + u.getUsername());
				} else if (LOG.isDebugEnabled()) {
					LOG.debug("Principal: " + principal);
				}
			}
			request.setAttribute("authentication", authentication);
		}
		LOG.debug("\n");
		ThreadContext.put(Constant.USERNAME, username);
		chain.doFilter(request, response);
		ThreadContext.remove(Constant.USERNAME);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {}

}
