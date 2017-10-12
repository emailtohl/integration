package com.github.emailtohl.integration.user.security;

import static com.github.emailtohl.integration.user.entities.Authority.AUDIT_ROLE;
import static com.github.emailtohl.integration.user.entities.Authority.AUDIT_USER;

import javax.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.jpa._Page;
import com.github.emailtohl.integration.user.dto.RoleDto;
import com.github.emailtohl.integration.user.dto.UserDto;
/**
 * 查询被审计的实体的历史记录
 * @author HeLei
 * @date 2017.02.04
 */
@Transactional
public interface AuditedService {
	/**
	 * 根据User的email查询某实体所有历史记录
	 * @param email 
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
	_Page<UserDto> getUserRevision(String email, Pageable pageable);
	
	/**
	 * 查询User某个修订版下所有的历史记录
	 * @param revision
	 * @param email
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
	_Page<UserDto> getUsersAtRevision(int revision, String email, Pageable pageable);
	
	/**
	 * 查询User在某个修订版时的历史记录
	 * @param userId
	 * @param revision
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
	UserDto getUserAtRevision(long userId, int revision);
	
	/**
	 * 根据Role的名字查询某实体所有历史记录
	 * @param name 实体属性名和属性值
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_ROLE + "')")
	_Page<RoleDto> getRoleRevision(String name, Pageable pageable);
	
	/**
	 * 查询Role修订版下所有的历史记录
	 * @param revision
	 * @param name
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_ROLE + "')")
	_Page<RoleDto> getRolesAtRevision(int revision, String name, Pageable pageable);
	
	/**
	 * 查询Role在某个修订版时的历史记录
	 * @param id
	 * @param revision
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_ROLE + "')")
	RoleDto getRoleAtRevision(long roleId, int revision);
	
}
