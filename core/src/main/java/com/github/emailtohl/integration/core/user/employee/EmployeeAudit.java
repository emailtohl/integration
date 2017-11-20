package com.github.emailtohl.integration.core.user.employee;

import com.github.emailtohl.integration.common.jpa.envers.AuditedRepository;
import com.github.emailtohl.integration.core.user.entities.Employee;
/**
 * Employee的历史信息
 * @author HeLei
 */
interface EmployeeAudit extends AuditedRepository<Employee> {
}
