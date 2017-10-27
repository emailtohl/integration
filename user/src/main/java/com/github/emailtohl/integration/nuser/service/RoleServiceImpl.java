package com.github.emailtohl.integration.nuser.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

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
@Service
public class RoleServiceImpl implements RoleService {
	@Inject RoleRepository roleRepository;
	@Inject AuthorityRepository authorityRepository;

	@Override
	public Role create(Role entity) {
		Role r = new Role();
		r.setName(entity.getName());
		r.setDescription(entity.getDescription());
		r.getAuthorities().addAll(entity.getAuthorities());
		addAuthorities(r, entity.getAuthorities());
		return roleRepository.save(entity);
	}

	@Override
	public boolean exist(String name, Object matcherValue) {
		return roleRepository.exist(name);
	}

	@Override
	public Role get(Long id) {
		return roleRepository.getOne(id);
	}

	@Override
	public Page<Role> query(Role params, Pageable pageable) {
		return roleRepository.queryForPage(params, pageable);
	}

	@Override
	public List<Role> query(Role params) {
		return roleRepository.queryForList(params);
	}

	@Override
	public Role update(Long id, Role newEntity) {
		Role target = roleRepository.findOne(id);
		if (target == null) {
			return null;
		}
		target.setName(newEntity.getName());
		target.setDescription(newEntity.getDescription());
		// 先解除双方关系
		for (Iterator<Authority> i = target.getAuthorities().iterator(); i.hasNext();) {
			Authority a = i.next();
			a.getRoles().remove(target);
			i.remove();
		}
		// 再重新添加
		addAuthorities(target, newEntity.getAuthorities());
		return target;
	}

	@Override
	public void delete(Long id) {
		Role target = roleRepository.findOne(id);
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
	}
	
	private void addAuthorities(Role r, Set<Authority> authorities) {
		authorities.stream().forEach(a -> {
			Authority p = null;
			if (a.getId() != null) {
				p = authorityRepository.findOne(a.getId());
			} else if (StringUtils.hasText(a.getName())) {
				p = authorityRepository.findByName(a.getName());
			}
			if (p != null) {
				r.getAuthorities().add(p);
			}
		});
	}
	
}
