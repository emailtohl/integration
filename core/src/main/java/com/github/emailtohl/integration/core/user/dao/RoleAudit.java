package com.github.emailtohl.integration.core.user.dao;

import com.github.emailtohl.integration.common.jpa.envers.AuditedRepository;
import com.github.emailtohl.integration.core.user.entities.Role;
/**
 * Role的历史信息
 * @author HeLei
 * @date 2017.02.04
 */
public interface RoleAudit extends AuditedRepository<Role> {
}
