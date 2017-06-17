package com.github.emailtohl.integration.user.dao;

import com.github.emailtohl.integration.common.jpa.envers.AuditedRepository;
import com.github.emailtohl.integration.user.entities.User;
/**
 * User的历史信息
 * @author HeLei
 * @date 2017.02.04
 */
public interface UserAudit extends AuditedRepository<User> {
}
