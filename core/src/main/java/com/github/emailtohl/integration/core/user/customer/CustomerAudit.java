package com.github.emailtohl.integration.core.user.customer;

import com.github.emailtohl.integration.common.jpa.envers.AuditedRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;

/**
 * Customer的历史信息
 * @author HeLei
 */
interface CustomerAudit extends AuditedRepository<Customer> {

}
