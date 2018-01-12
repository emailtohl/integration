package com.github.emailtohl.integration.web.aop;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Picture;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.file.Image;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 为Activiti的IdentityService生成代理，在修改用户信息时，同步数据到Activiti库中
 * 考虑做成切面的原因有二：1.不侵入式的修改业务系统的用户管理逻辑；2.在切面中可以共享统一事务。
 * 
 * 在业务系统中，标识Customer有邮箱、手机，而且它们还可以修改，根据id不变性（若改变了，将其作为外键的所有关联将会错误）Activiti的User里也是用业务系统User中的id
 * 尽管使用数字id不便于在流程部署时指定具体签收人，但是考虑到业务上一般是指定用户组（角色），暂时可以接受
 * 
 * 另外，在Activiti的User的FirstName里，存储的是业务系统User的name，LastName里，存储的是业务系统User的nickname，便于界面展示。
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
				org.activiti.engine.identity.User user = saveUser(u, false);
				LOG.debug("添加到identityService中:" + user);
			}
		}
	}
	
	@AfterReturning(value = "execution(* com.github.emailtohl.integration.core.user.*.*.update(..))", returning = "returnVal")
	public void update(Object returnVal) throws Throwable {
		if (returnVal instanceof User) {
			User u = (User) returnVal;
			if (u.getId() != null) {
				org.activiti.engine.identity.User user = saveUser(u, false);
				LOG.debug("更新到identityService中:" + user);
			}
		}
	}
	
	@AfterReturning(value = "execution(* com.github.emailtohl.integration.core.user.*.*.grandRoles(..))", returning = "returnVal")
	public void grandRoles(Object returnVal) throws Throwable {
		if (returnVal instanceof User) {
			User u = (User) returnVal;
			if (u.getId() != null) {
				org.activiti.engine.identity.User user = saveUser(u, true);
				LOG.debug("更新到identityService中:" + user);
			}
		}
	}
	
	@Around(value = "execution(* com.github.emailtohl.integration.core.user.*.*.delete(..))")
	public Object delete(ProceedingJoinPoint jp) throws Throwable {
		Object res = null;
		Object[] args = jp.getArgs();
		if (args != null && args.length == 1 && args[0] instanceof Long) {
			String userId = args[0].toString();
			// 执行业务，根据实际返回的结果进行同步
			res = jp.proceed();
			// identityService能同时删除与组的关系
			identityService.deleteUser(userId);
		} else {
			res = jp.proceed();
		}
		return res;
	}
	
	@AfterReturning(value = "execution(* com.github.emailtohl.integration.core.user.*.*.*Password(..))", returning = "returnVal")
	public void updatePassword(Object returnVal) throws Throwable {
		if (returnVal instanceof ExecResult) {
			ExecResult er = (ExecResult) returnVal;
			if (er.ok && er.attribute instanceof User) {
				User u = (User) er.attribute;
				if (u.getId() != null) {
					saveUser(u, false);
				}
			}
		}
	}
	
	
	/**
	 * 若查询不到org.activiti.engine.identity.User，则创建一个再返回
	 * @param u 业务系统中的User
	 * @param membership 是否同时维护与组的关系
	 * @return org.activiti.engine.identity.User
	 */
	private org.activiti.engine.identity.User saveUser(User u, boolean membership) {
		String userId = u.getId().toString();
		org.activiti.engine.identity.User user = identityService.createUserQuery().userId(userId).singleResult();
		if (user == null) {
			user = identityService.newUser(userId);
		}
		// user.setId(u.getId().toString());
		user.setEmail(u.getEmail());
		user.setFirstName(u.getName());
		user.setLastName(u.getNickname());
		user.setPassword(u.getPassword());
		identityService.saveUser(user);
		
		setUserPicture(u);
		
		if (membership) {
			// 查找原先该用户关联的组id
			Set<String> preGroupIds = identityService.createGroupQuery().groupMember(userId).list().stream()
					.map(group -> group.getId()).collect(Collectors.toSet());
			// 用户应该关联的组id
			Set<String> newGroupIds = u.getRoles().stream().filter(r -> r.getName() != null).map(r -> r.getName())
					.collect(Collectors.toSet());
			
			// 删除没有的关系
			preGroupIds.forEach(groupId -> {
				if (!newGroupIds.contains(groupId)) {
					identityService.deleteMembership(userId, groupId);
				}
			});
			// 创建新的关系
			newGroupIds.forEach(groupId -> {
				if (!preGroupIds.contains(groupId)) {
					identityService.createMembership(userId, groupId);
				}
			});
		}
		return user;
	}
	
	private void setUserPicture(User u) {
		Image img = u.getImage();
		if (img != null && img.getBin() != null && StringUtils.hasText(img.getSrc())) {
			String mimeType = FilenameUtils.getExtension(img.getSrc());
			mimeType = "image/" + mimeType;
			Picture pic = new Picture(img.getBin(), mimeType);
			identityService.setUserPicture(u.getId().toString(), pic);
		}
	}
}
