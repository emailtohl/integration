package com.github.emailtohl.integration.core.role;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.common.jpa.envers.AbstractAuditedRepository;
import com.github.emailtohl.integration.core.role.Role;
/**
 * Role的历史信息
 * @author HeLei
 * @date 2017.02.04
 */
@Repository
class RoleAuditImpl extends AbstractAuditedRepository<Role> implements RoleAudit {

}
