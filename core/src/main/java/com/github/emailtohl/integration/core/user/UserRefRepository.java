package com.github.emailtohl.integration.core.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.user.entities.UserRef;

/**
 * 用户引用的查询数据层
 * @author HeLei
 */
public interface UserRefRepository extends JpaRepository<UserRef, Long> {

}
