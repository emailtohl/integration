package com.github.emailtohl.integration.core.user.employee;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.lib.jpa.AuditedRepository;
/**
 * Employee的历史信息
 * @author HeLei
 */
@Repository
class EmployeeAuditImpl extends AuditedRepository<Employee, Long> implements EmployeeAudit {

}
