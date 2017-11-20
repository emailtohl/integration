package com.github.emailtohl.integration.core.role;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.core.role.Authority;

/**
 * 授权访问接口
 * @author HeLei
 */
interface AuthorityRepository extends JpaRepository<Authority, Long> {
	String CACHE_NAME = "authorityCache";

	@Cacheable(CACHE_NAME)
	Authority findByName(String name);
}
