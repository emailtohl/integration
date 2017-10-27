package com.github.emailtohl.integration.nuser.service;

import static com.github.emailtohl.integration.nuser.entities.Authority.*;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import com.github.emailtohl.integration.common.standard.StandardService;
import com.github.emailtohl.integration.nuser.entities.Authority;
import com.github.emailtohl.integration.nuser.entities.Role;
/**
 * 角色管理的服务层
 * @author HeLei
 */
@PreAuthorize("hasAuthority('" + ROLE + "')")
@Transactional
@Validated
public interface RoleService extends StandardService<Role> {
	
}
