package com.github.emailtohl.integration.nuser.dao;

import com.github.emailtohl.integration.common.jpa.envers.AuditedRepository;
import com.github.emailtohl.integration.nuser.entities.User;
/**
 * 用户的数据审计访问接口
 * @author HeLei
 */
public interface UserAudited extends AuditedRepository<User> {

}
