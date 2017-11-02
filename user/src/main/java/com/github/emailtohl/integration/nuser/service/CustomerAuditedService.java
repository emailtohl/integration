package com.github.emailtohl.integration.nuser.service;

import static com.github.emailtohl.integration.nuser.entities.Authority.AUDIT_USER;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.nuser.entities.Customer;

/**
 * 查询被审计的外部账户的历史记录
 * @author HeLei
 */
@PreAuthorize("hasAuthority('" + AUDIT_USER + "')")
public interface CustomerAuditedService {
	
	/**
	 * 查询内部账户所有的历史记录
	 * @param id 内部账户id
	 * @return 元组列表，元组中包含版本详情，实体在该版本时的状态以及该版本的操作（增、改、删）
	 */
	List<Tuple<Customer>> getCustomerRevision(Long id);
	
	/**
	 * 查询内部账户在某个修订版时的历史记录
	 * @param id 内部账户的id
	 * @param revision 版本号，通过AuditReader#getRevisions(Customer.class, ID)获得
	 * @return
	 */
	Customer getCustomerAtRevision(Long id, Number revision);
	
}