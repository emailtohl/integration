package com.github.emailtohl.integration.nuser.service;

import static com.github.emailtohl.integration.nuser.entities.Authority.AUDIT_ROLE;
import static com.github.emailtohl.integration.nuser.entities.Authority.AUDIT_USER;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.nuser.entities.Role;
import com.github.emailtohl.integration.nuser.entities.User;
/**
 * 查询被审计的实体的历史记录
 * @author HeLei
 */
public interface AuditedService {
	/**
	 * 根据User的email查询某实体所有历史记录
	 * @param id 
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
	Page<User> getUserRevision(Long id, Pageable pageable);
	
	/**
	 * 查询User某个修订版下所有的历史记录
	 * @param revision
	 * @param id
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
	Page<User> getUsersAtRevision(int revision, Long id, Pageable pageable);
	
	/**
	 * 查询User在某个修订版时的历史记录
	 * @param id
	 * @param revision
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
	User getUserAtRevision(Long id, int revision);
	
	/**
	 * 根据Role的名字查询某实体所有历史记录
	 * @param name 实体属性名和属性值
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_ROLE + "')")
	Page<Role> getRoleRevision(String name, Pageable pageable);
	
	/**
	 * 查询Role修订版下所有的历史记录
	 * @param revision
	 * @param name
	 * @param pageable
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_ROLE + "')")
	Page<Role> getRolesAtRevision(int revision, String name, Pageable pageable);
	
	/**
	 * 查询Role在某个修订版时的历史记录
	 * @param id
	 * @param revision
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + AUDIT_ROLE + "')")
	Role getRoleAtRevision(long roleId, int revision);
	
}
