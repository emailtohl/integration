package com.github.emailtohl.integration.core.role;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 角色访问接口
 * @author HeLei
 */
public interface RoleRepository extends JpaRepository<Role, Long>, RoleRepositoryCustomization {
	Role findByName(String name);
}
