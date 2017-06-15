package com.github.emailtohl.integration.common.jpa.testRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.common.testEntities.Role;
/**
 * 角色访问接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
	Role findByName(String name);
}
