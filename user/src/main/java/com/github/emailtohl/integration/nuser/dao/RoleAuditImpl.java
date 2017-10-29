package com.github.emailtohl.integration.nuser.dao;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.common.jpa.envers.AbstractAuditedRepository;
import com.github.emailtohl.integration.nuser.entities.Role;
/**
 * Role的历史信息
 * @author HeLei
 */
@Repository
public class RoleAuditImpl extends AbstractAuditedRepository<Role> implements RoleAudit {

}
