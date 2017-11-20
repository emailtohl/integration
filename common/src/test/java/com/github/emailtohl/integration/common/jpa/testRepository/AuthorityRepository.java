package com.github.emailtohl.integration.common.jpa.testRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.common.testEntities.Authority;
/**
 * 授权访问接口
 * @author HeLei
 */
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
	String CACHE_NAME = "authorityCache";

	@Cacheable(CACHE_NAME)
	Authority findByName(String name);
}
