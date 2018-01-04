package com.github.emailtohl.integration.web.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.activiti.engine.IdentityService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.auth.UserDetailsImpl;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.user.entities.Customer;
/**
 * 在spring security过滤器之后执行
 * 
 * 在此处设置线程上下文的用户属性，是考虑到或许当前用户信息是来自于前端，如JWT
 * 
 * Servlet Filter implementation class PostSecurityLoggingFilter
 * @author HeLei
 */
//@WebFilter("/*")
public class PostSecurityLoggingFilter implements Filter {
	public static final Logger LOG = LogManager.getLogger();
	
	@Inject
	IdentityService identityService;
	@Inject
	EntityManagerFactory entityManagerFactory;
	
	Long anonymousId;
	
	/**
	 * Default constructor.
	 */
	public PostSecurityLoggingFilter() {
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(fConfig.getServletContext());
		AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
		factory.autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		factory.initializeBean(this, "PostSecurityLoggingFilter");
		// 直接访问数据库，避开初始化时各种校验层的影响
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		CriteriaBuilder b = em.getCriteriaBuilder();
		CriteriaQuery<Long> q = b.createQuery(Long.class);
		Root<Customer> r = q.from(Customer.class);
		q = q.select(r.get("id")).where(b.equal(r.<String>get("email"), Constant.ANONYMOUS_EMAIL));
		anonymousId = em.createQuery(q).getSingleResult();
		em.getTransaction().commit();
		em.close();
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
		Long userId = anonymousId;
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			Authentication authentication = context.getAuthentication();
			if (authentication != null && StringUtils.hasText(authentication.getName())) {
				username = authentication.getName();
				Object principal = authentication.getPrincipal();
				if (principal instanceof UserDetailsImpl) {
					UserDetailsImpl u = (UserDetailsImpl) principal;
					if (u.getId() != null) {
						userId = u.getId();
					}
					LOG.debug("principal: {}", u);
				}
			}
		}
		StandardService.CURRENT_USERNAME.set(username);
		ThreadContext.put(Constant.USERNAME, username);
		if (userId != null) {
			identityService.setAuthenticatedUserId(userId.toString());
		}
		
		chain.doFilter(request, response);
		
		StandardService.CURRENT_USERNAME.remove();
		ThreadContext.remove(Constant.USERNAME);
	}

}
