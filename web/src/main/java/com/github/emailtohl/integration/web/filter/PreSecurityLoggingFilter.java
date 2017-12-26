package com.github.emailtohl.integration.web.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.util.WebUtils;

import com.github.emailtohl.integration.core.config.Constant;
/**
 * 在spring security过滤器之前执行
 * Servlet Filter implementation class PreSecurityLoggingFilter
 * @author HeLei
 */
//@WebFilter("/*")
@SuppressWarnings("unused")
public class PreSecurityLoggingFilter implements Filter {
	public static final String ID_PROPERTY_NAME = "id";
	public static final String REQUEST_ID_PROPERTY_NAME = "Request-Id";
	
	/**
	 * Default constructor.
	 */
	public PreSecurityLoggingFilter() {
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
		String id = UUID.randomUUID().toString();
		ThreadContext.put(ID_PROPERTY_NAME, id);
		try {
			HttpServletRequest req = (HttpServletRequest) request;
			((HttpServletResponse) response).setHeader(REQUEST_ID_PROPERTY_NAME, id);
			ThreadContext.put(Constant.REMOTE_ADDRESS_PROPERTY_NAME, req.getRemoteAddr());
			ThreadContext.put(Constant.SESSION_ID_PROPERTY_NAME, req.getRequestedSessionId());
			ThreadContext.put(Constant.USER_PRINCIPAL_PROPERTY_NAME, req.getUserPrincipal() == null ? "" : req.getUserPrincipal().toString());
			chain.doFilter(request, response);
		} finally {
			ThreadContext.remove(ID_PROPERTY_NAME);
			ThreadContext.remove(REQUEST_ID_PROPERTY_NAME);
			ThreadContext.remove(Constant.REMOTE_ADDRESS_PROPERTY_NAME);
			ThreadContext.remove(Constant.SESSION_ID_PROPERTY_NAME);
			ThreadContext.remove(Constant.USER_PRINCIPAL_PROPERTY_NAME);
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
