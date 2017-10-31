package com.github.emailtohl.integration.nuser.dao;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.common.jpa.envers.AbstractAuditedRepository;
import com.github.emailtohl.integration.nuser.entities.Employee;
/**
 * Employee的历史信息
 * @author HeLei
 */
@Repository
public class EmployeeAuditImpl extends AbstractAuditedRepository<Employee> implements EmployeeAudit {

}
