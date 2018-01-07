package com.github.emailtohl.integration.core.role;

import static com.github.emailtohl.integration.core.role.Authority.ROLE;

import java.util.List;

import javax.annotation.security.PermitAll;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.jpa.Paging;

/**
 * 角色管理的服务层
 * 覆盖StandardService中的方法是因为要标注安全层校验
 * @author HeLei
 */
public interface RoleService {
	/**
	 * 创建一个角色
	 * @param entity
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + ROLE + "')")
	Role create(Role entity);
	
	/**
	 * 根据角色名查找是否已存在
	 * @param matcherValue
	 * @return
	 */
	boolean exist(Object matcherValue);
	
	/**
	 * 根据ID获取角色
	 * @param id
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Role get(Long id);
	
	/**
	 * 根据ID获取角色
	 * @param id
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	Role get(String roleName);
	
	/**
	 * 分页查询
	 * @param params
	 * @param pageable
	 * @return Paging封装的分页信息，一般JPA底层返回的是Page对象，但该对象不利于JSON等序列化。
	 * 所以在将持久化状态的用户转瞬态时，同时改变分页对象
	 */
	@PreAuthorize("isAuthenticated()")
	Paging<Role> query(Role params, Pageable pageable);

	/**
	 * 查询列表
	 * @param params
	 * @return
	 */
	@PreAuthorize("isAuthenticated()")
	List<Role> query(Role params);
	
	/**
	 * 修改角色内容，但不能修改角色名
	 * @param id
	 * @param newEntity
	 * @return 返回null表示没找到该角色
	 */
	@PreAuthorize("hasAuthority('" + ROLE + "')")
	Role update(Long id, Role newEntity);

	/**
	 * 根据ID删除角色
	 * @param id
	 */
	@PreAuthorize("hasAuthority('" + ROLE + "')")
	void delete(Long id);
	
	/**
	 * 获取所有权限
	 * @return
	 */
	List<Authority> getAuthorities();
	
	/**
     * 通过id查找角色的名字
     * @param id 角色id
     * @return 角色名
     */
	@PermitAll
    String getRoleName(Long id);
}
