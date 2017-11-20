package com.github.emailtohl.integration.core.user.employee;

import static com.github.emailtohl.integration.core.role.Authority.AUDIT_USER;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.user.entities.Employee;
/**
 * 查询被审计的平台账号的历史记录
 * @author HeLei
 */
@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
public interface EmployeeAuditedService {
	
	/**
	 * 查询平台账号所有的历史记录
	 * @param id 平台账号id
	 * @return 元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	List<Tuple<Employee>> getEmployeeRevision(Long id);
	
	/**
	 * 查询平台账号在某个修订版时的历史记录
	 * @param id 平台账号的id
	 * @param revision 版本号，通过AuditReader#getRevisions(Employee.class, ID)获得
	 * @return
	 */
	Employee getEmployeeAtRevision(Long id, Number revision);
	
}
