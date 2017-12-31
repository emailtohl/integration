package com.github.emailtohl.integration.web.aop;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.github.emailtohl.integration.core.role.Role;

/**
 * 为Activiti的IdentityService生成代理，在修改角色（组）信息时，同步数据到Activiti库中
 * 考虑做成切面的原因有二：1.不侵入式的修改业务系统的角色（组）管理逻辑；2.在切面中可以共享统一事务。
 * 
 * @author HeLei
 *
 */
@Component
@Aspect
@Transactional
public class RoleServiceProxy {
	private static final Logger LOG = LogManager.getLogger();
	@Inject
	IdentityService identityService;
	
	@AfterReturning(value = "execution(* com.github.emailtohl.integration.core.role.RoleService.create(..))", returning = "returnVal")
	public void create(Object returnVal) throws Throwable {
		if (returnVal instanceof Role) {
			Role r = (Role) returnVal;
			if (r.getId() != null) {
				Group g = saveGroup(r);
				LOG.debug("添加到identityService中:" + g);
			}
		}
	}
	
	@AfterReturning(value = "execution(* com.github.emailtohl.integration.core.role.RoleService.update(..))", returning = "returnVal")
	public void update(Object returnVal) throws Throwable {
		if (returnVal instanceof Role) {
			Role r = (Role) returnVal;
			if (r.getId() != null) {
				Group g = saveGroup(r);
				LOG.debug("更新到identityService中:" + g);
			}
		}
	}
	
	@Around(value = "execution(* com.github.emailtohl.integration.core.role.RoleService.delete(..))")
	public Object delete(ProceedingJoinPoint jp) throws Throwable {
		Object res = null;
		Object[] args = jp.getArgs();
		if (args != null && args.length == 1 && args[0] instanceof Long) {
			String roleId = args[0].toString();
			// 执行业务，根据实际返回的结果进行同步
			res = jp.proceed();
			identityService.deleteGroup(roleId);
		} else {
			res = jp.proceed();
		}
		return res;
	}
	
	/**
	 * 若查询不到org.activiti.engine.identity.Group，则创建一个再返回
	 * @param r 业务系统中的Role，一定含id属性
	 * @return org.activiti.engine.identity.Group
	 */
	private Group saveGroup(Role r) {
		String roleId = r.getId().toString();
		Group g = identityService.createGroupQuery().groupId(roleId).singleResult();
		if (g == null) {
			g = identityService.newGroup(roleId);
		}
		// g.setId(roleId);
		g.setName(r.getName());
		g.setType(r.getRoleType().name());
		identityService.saveGroup(g);
		return g;
	}
}
