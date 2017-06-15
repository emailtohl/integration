package com.github.emailtohl.integration.user.dao;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.emailtohl.integration.user.entities.Authority;
/**
 * 授权访问接口
 * @author HeLei
 * @date 2017.02.04
 */
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
	String CACHE_NAME = "authorityCache";

	@Cacheable(CACHE_NAME)
	Authority findByName(String name);
}
