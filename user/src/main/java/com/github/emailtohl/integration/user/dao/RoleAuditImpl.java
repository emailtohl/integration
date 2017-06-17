package com.github.emailtohl.integration.user.dao;

import org.springframework.stereotype.Repository;

import com.github.emailtohl.integration.common.jpa.envers.AbstractAuditedRepository;
import com.github.emailtohl.integration.user.entities.Role;
/**
 * Role的历史信息
 * @author HeLei
 * @date 2017.02.04
 */
@Repository
public class RoleAuditImpl extends AbstractAuditedRepository<Role> implements RoleAudit {

}
