package com.github.emailtohl.integration.core.user.service;

import static com.github.emailtohl.integration.core.role.Authority.AUDIT_USER;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.user.entities.Customer;

/**
 * 查询被审计的客户的历史记录
 * @author HeLei
 */
@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
public interface CustomerAuditedService {
	
	/**
	 * 查询客户所有的历史记录
	 * @param id 平台账号id
	 * @return 元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	List<Tuple<Customer>> getCustomerRevision(Long id);
	
	/**
	 * 查询客户在某个修订版时的历史记录
	 * @param id 客户的id
	 * @param revision 版本号，通过AuditReader#getRevisions(Customer.class, ID)获得
	 * @return
	 */
	Customer getCustomerAtRevision(Long id, Number revision);
	
}
