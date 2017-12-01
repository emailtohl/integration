package com.github.emailtohl.integration.core.role;

import static com.github.emailtohl.integration.core.role.Authority.ROLE;

import java.util.List;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.core.StandardService;
/**
 * 角色管理的服务层
 * 覆盖StandardService中的方法是因为要标注安全层校验
 * @author HeLei
 */
@Validated
public interface RoleService extends StandardService<Role> {
	/**
	 * 创建一个角色
	 * @param entity
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + ROLE + "')")
	@Override
	Role create(@Valid Role entity);
	
	/**
	 * 修改角色内容，并指明哪些属性忽略
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该角色
	 */
	@PreAuthorize("hasAuthority('" + ROLE + "')")
	@Override
	Role update(Long id, @Valid Role newEntity);

	/**
	 * 根据ID删除角色
	 * @param id
	 */
	@PreAuthorize("hasAuthority('" + ROLE + "')")
	@Override
	void delete(Long id);
	
	/**
	 * 获取所有权限
	 * @return
	 */
	List<Authority> getAuthorities();
}
