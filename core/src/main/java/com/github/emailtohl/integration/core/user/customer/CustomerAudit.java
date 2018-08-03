package com.github.emailtohl.integration.core.user.customer;

import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.lib.jpa.AuditedInterface;

/**
 * Customer的历史信息
 * @author HeLei
 */
interface CustomerAudit extends AuditedInterface<Customer, Long> {

}
