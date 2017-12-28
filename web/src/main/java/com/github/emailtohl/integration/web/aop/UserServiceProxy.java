package com.github.emailtohl.integration.web.aop;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.activiti.engine.IdentityService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 为Activiti的IdentityService生成代理，在修改用户信息时，同步数据到Activiti库中
 * 
 * @author HeLei
 *
 */
@Component
@Aspect
@Transactional
public class UserServiceProxy {
	private static final Logger LOG = LogManager.getLogger();
	@Inject
	IdentityService identityService;
	
	@AfterReturning(value = "execution(* com.github.emailtohl.integration.core.user.*.*.create(..))", returning = "returnVal")
	public void create(Object returnVal) throws Throwable {
		if (returnVal instanceof User) {
			User u = (User) returnVal;
			if (u.getId() != null) {
				org.activiti.engine.identity.User user = identityService.newUser(u.getId().toString());
				user.setId(u.getId().toString());
				user.setEmail(u.getEmail());
				user.setFirstName(u.getName());
				user.setLastName(u.getNickname());
				user.setPassword(u.getPassword());
				LOG.debug("添加到identityService中:" + user);
				identityService.saveUser(user);
			}
		}
	}
	
	@Around("execution(* com.github.emailtohl.integration.core.user.*.*.update(..))")
	public Object update(ProceedingJoinPoint jp) throws Throwable {
		Object res = null;
		Object[] args = jp.getArgs();
		if (args != null && args.length == 2 && args[1] instanceof User) {
			User u = (User) args[1];
			Set<String> roleNames = getRoleNames(u);
			// 执行业务
			res = jp.proceed();
			if (res instanceof User) {
				u = (User) res;
				if (u.getId() != null) {
					String userId = u.getId().toString();
					org.activiti.engine.identity.User user = identityService.createUserQuery()
							.userId(u.getId().toString()).singleResult();
					if (user == null) {
						user = identityService.newUser(u.getId().toString());
					}
					user.setId(userId);
					user.setEmail(u.getEmail());
					user.setFirstName(u.getName());
					user.setLastName(u.getNickname());
					user.setPassword(u.getPassword());
					LOG.debug("修改用户同步到identityService中：" + user);
					identityService.saveUser(user);

					roleNames.forEach(roleName -> identityService.deleteMembership(userId, roleName));
					Set<String> newRoleNames = getRoleNames(u);
					newRoleNames.forEach(roleName -> identityService.createMembership(userId, roleName));
				}
			}
		} else {
			res = jp.proceed();
		}
		return res;
	}
	/*
	@AfterReturning(value = "execution(* com.github.emailtohl.integration.core.user.*.*.create(..))", returning = "returnVal")
	public void delete(JoinPoint jp) throws Throwable {
		
		long start = System.currentTimeMillis();
		Object res = jp.proceed();
		long end = System.currentTimeMillis();
		long interval = end - start;
		LOG.trace(jp.getSignature() + " : " + interval + " 毫秒");
		return res;
	}
	
	@Around("execution(* com.github.emailtohl.integration.core.user.*.*.grandRoles(..))")
	public Object grandRoles(ProceedingJoinPoint jp) throws Throwable {
		
		long start = System.currentTimeMillis();
		Object res = jp.proceed();
		long end = System.currentTimeMillis();
		long interval = end - start;
		LOG.trace(jp.getSignature() + " : " + interval + " 毫秒");
		return res;
	}*/
	
	private Set<String> getRoleNames(User u) {
		return u.getRoles().stream().filter(r -> r.getName() != null).map(r -> r.getName())
				.collect(Collectors.toSet());
	}
}
