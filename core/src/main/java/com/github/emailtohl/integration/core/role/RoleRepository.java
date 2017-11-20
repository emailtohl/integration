package com.github.emailtohl.integration.core.role;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.role.Role;

/**
 * 角色访问接口
 * @author HeLei
 */
public interface RoleRepository extends JpaRepository<Role, Long>, RoleRepositoryCustomization {
	Role findByName(String name);
}
