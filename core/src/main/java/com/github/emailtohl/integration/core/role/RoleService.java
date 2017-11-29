package com.github.emailtohl.integration.core.role;

import static com.github.emailtohl.integration.core.role.Authority.ROLE;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.role.Authority;
import com.github.emailtohl.integration.core.role.Role;
/**
 * 角色管理的服务层
 * @author HeLei
 */
@PreAuthorize("hasAuthority('" + ROLE + "')")
@Validated
public interface RoleService extends StandardService<Role> {
	/**
	 * 获取所有权限
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	List<Authority> getAuthorities();
}
