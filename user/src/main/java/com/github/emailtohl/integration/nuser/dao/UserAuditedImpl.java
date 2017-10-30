package com.github.emailtohl.integration.nuser.dao;

import com.github.emailtohl.integration.common.jpa.envers.AbstractAuditedRepository;
import com.github.emailtohl.integration.nuser.entities.User;
/**
 * 用户审计接口实现
 * @author HeLei
 */
public class UserAuditedImpl extends AbstractAuditedRepository<User> implements UserAudited {

}
