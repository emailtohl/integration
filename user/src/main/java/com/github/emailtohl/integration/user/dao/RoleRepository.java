package com.github.emailtohl.integration.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.user.entities.Role;
/**
 * 角色访问接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface RoleRepository extends JpaRepository<Role, Long>, RoleRepositoryCustomization {
	Role findByName(String name);
}
