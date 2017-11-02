package com.github.emailtohl.integration.nuser.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.nuser.entities.Role;

/**
 * 角色访问接口
 * @author HeLei
 */
public interface RoleRepository extends JpaRepository<Role, Long>, RoleRepositoryCustomization {
	Role findByName(String name);
}