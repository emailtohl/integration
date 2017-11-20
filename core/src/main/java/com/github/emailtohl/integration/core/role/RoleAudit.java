package com.github.emailtohl.integration.core.role;

import com.github.emailtohl.integration.common.jpa.envers.AuditedRepository;
import com.github.emailtohl.integration.core.role.Role;
/**
 * Role的历史信息
 * @author HeLei
 */
interface RoleAudit extends AuditedRepository<Role> {
}
