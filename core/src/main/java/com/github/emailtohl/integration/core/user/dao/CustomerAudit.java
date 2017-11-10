package com.github.emailtohl.integration.core.user.dao;

import com.github.emailtohl.integration.common.jpa.envers.AuditedRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;

/**
 * Customer的历史信息
 * @author HeLei
 */
public interface CustomerAudit extends AuditedRepository<Customer> {

}
