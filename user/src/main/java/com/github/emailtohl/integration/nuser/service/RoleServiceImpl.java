package com.github.emailtohl.integration.nuser.service;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.nuser.dao.AuthorityRepository;
import com.github.emailtohl.integration.nuser.dao.RoleRepository;
import com.github.emailtohl.integration.nuser.entities.Authority;
import com.github.emailtohl.integration.nuser.entities.Role;
import com.github.emailtohl.integration.nuser.entities.User;
/**
 * 角色管理服务的实现
 * @author HeLei
 */
@Transactional
@Service
public class RoleServiceImpl implements RoleService {
	private static final String CACHE_NAME_ROLE = "roleCache";
	private static final String CACHE_NAME_AUTHORITY = "authorityListCache";
	@Inject RoleRepository roleRepository;
	@Inject AuthorityRepository authorityRepository;

	@CachePut(value = CACHE_NAME_ROLE, key = "#result.id")
	@Override
	public Role create(Role entity) {
		Role r = new Role();
		r.setName(entity.getName());
		r.setDescription(entity.getDescription());
		entity.getAuthorities().forEach(a -> {
			Authority p = null;
			if (a.getId() != null) {
				p = authorityRepository.findOne(a.getId());
			} else if (StringUtils.hasText(a.getName())) {
				p = authorityRepository.findByName(a.getName());
			}
			if (p != null) {
				r.getAuthorities().add(p);
				p.getRoles().add(r);
			}
		});
		return roleRepository.save(r);
	}

	@Override
	public boolean exist(String name, Object matcherValue) {
		return roleRepository.exist((String) matcherValue);
	}

	@Cacheable(value = CACHE_NAME_ROLE, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Role get(Long id) {
		Role r = roleRepository.get(id);
		if (r != null) {
			r.getAuthorities().isEmpty();// 关联查询
		}
		return r;
	}

	@Override
	public Page<Role> query(Role params, Pageable pageable) {
		return roleRepository.queryForPage(params, pageable);
	}

	@Override
	public List<Role> query(Role params) {
		return roleRepository.queryForList(params);
	}

	@CachePut(value = CACHE_NAME_ROLE, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Role update(Long id, Role newEntity) {
		Role target = roleRepository.get(id);
		if (target == null) {
			return null;
		}
		if (StringUtils.hasText(newEntity.getName())) {
			target.setName(newEntity.getName());
		}
		target.setDescription(newEntity.getDescription());
		// 先解除双方关系
		for (Iterator<Authority> i = target.getAuthorities().iterator(); i.hasNext();) {
			Authority a = i.next();
			a.getRoles().remove(target);
			i.remove();
		}
		// 再重新添加
		newEntity.getAuthorities().forEach(a -> {
			Authority p = null;
			if (a.getId() != null) {
				p = authorityRepository.findOne(a.getId());
			} else if (StringUtils.hasText(a.getName())) {
				p = authorityRepository.findByName(a.getName());
			}
			if (p != null) {
				target.getAuthorities().add(p);
				p.getRoles().add(target);
			}
		});
		return target;
	}

	@CacheEvict(value = CACHE_NAME_ROLE, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Role target = roleRepository.get(id);
		if (target == null) {
			return;
		}
		// 先解除双方关系
		for (Iterator<Authority> i = target.getAuthorities().iterator(); i.hasNext();) {
			Authority a = i.next();
			a.getRoles().remove(target);
			i.remove();
		}
		for (Iterator<User> i = target.getUsers().iterator(); i.hasNext();) {
			User a = i.next();
			a.getRoles().remove(target);
			i.remove();
		}
		roleRepository.delete(id);
	}

	@Cacheable(value = CACHE_NAME_AUTHORITY)
	@Override
	public List<Authority> getAuthorities() {
		return authorityRepository.findAll();
	}
	
}
