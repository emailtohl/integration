package com.github.emailtohl.integration.core.role;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.lib.jpa.AuditedRepository;
/**
 * Role的历史信息
 * @author HeLei
 * @date 2017.02.04
 */
@Repository
class RoleAuditImpl extends AuditedRepository<Role, Long> implements RoleAudit {

}
