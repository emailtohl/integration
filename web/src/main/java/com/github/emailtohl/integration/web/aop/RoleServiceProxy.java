package com.github.emailtohl.integration.web.aop;

import javax.transaction.Transactional;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

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

}
