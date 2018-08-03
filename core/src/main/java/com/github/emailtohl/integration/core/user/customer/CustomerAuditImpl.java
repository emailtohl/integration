package com.github.emailtohl.integration.core.user.customer;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.lib.jpa.AuditedRepository;

/**
 * Customer的历史信息
 * @author HeLei
 */
@Repository
class CustomerAuditImpl extends AuditedRepository<Customer, Long> implements CustomerAudit {

}
