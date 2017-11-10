package com.github.emailtohl.integration.core.user.dao;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.common.jpa.envers.AbstractAuditedRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;

/**
 * Customer的历史信息
 * @author HeLei
 */
@Repository
public class CustomerAuditImpl extends AbstractAuditedRepository<Customer> implements CustomerAudit {

}
