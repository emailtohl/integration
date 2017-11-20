package com.github.emailtohl.integration.core.role;

import static com.github.emailtohl.integration.core.role.Authority.AUDIT_ROLE;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.role.Role;

/**
 * 审计角色的历史记录
 * @author HeLei
 */
@PreAuthorize("hasAuthority('" + AUDIT_ROLE + "')")
public interface RoleAuditedService {
	/**
	 * 查询角色所有的历史记录
	 * @param id 角色
	 * @return 元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	List<Tuple<Role>> getRoleRevision(Long id);
	
	/**
	 * 查询角色在某个修订版时的历史记录
	 * @param id 角色id
	 * @param revision 版本号，通过AuditReader#getRevisions(Employee.class, ID)获得
	 * @return
	 */
	Role getRoleAtRevision(Long id, Number revision);
	
	/**
	 * 将角色回滚到某历史版本上
	 */
	void rollback(Long id, Number revision);
}
