package com.github.emailtohl.integration.nuser.dao;

import com.github.emailtohl.integration.common.jpa.envers.AuditedRepository;
import com.github.emailtohl.integration.nuser.entities.Customer;

/**
 * Customer的历史信息
 * @author HeLei
 */
public interface CustomerAudit extends AuditedRepository<Customer> {

}
