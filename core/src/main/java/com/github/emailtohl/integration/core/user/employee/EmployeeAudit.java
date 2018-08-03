package com.github.emailtohl.integration.core.user.employee;

import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.lib.jpa.AuditedInterface;
/**
 * Employee的历史信息
 * @author HeLei
 */
interface EmployeeAudit extends AuditedInterface<Employee, Long> {
}
