package com.github.emailtohl.integration.core.role;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.user.entities.User;
/**
 * 角色管理服务的实现
 * @author HeLei
 */
@Transactional
@Service
public class RoleServiceImpl extends StandardService<Role> implements RoleService {
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
			} else if (hasText(a.getName())) {
				p = authorityRepository.findByName(a.getName());
			}
			if (p != null) {
				r.getAuthorities().add(p);
				p.getRoles().add(r);
			}
		});
		return toTransient(roleRepository.save(r));
	}

	@Override
	public boolean exist(Object matcherValue) {
		return roleRepository.exist((String) matcherValue);
	}

	@Cacheable(value = CACHE_NAME_ROLE, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Role get(Long id) {
		Role r = roleRepository.get(id);
		if (r == null) {
			return null;
		}
		return transientDetail(r);
	}

	@Override
	public Role get(String roleName) {
		Role r = roleRepository.findByName(roleName);
		if (r == null) {
			return null;
		}
		return transientDetail(r);
	}

	@Override
	public Paging<Role> query(Role params, Pageable pageable) {
		Page<Role> p;
		if (params == null) {
			p = roleRepository.findAll(pageable);
		} else {
			p = roleRepository.queryForPage(params, pageable);
		}
		List<Role> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, p.getTotalElements());
	}

	@Override
	public List<Role> query(Role params) {
		List<Role> ls;
		if (params == null) {
			ls = roleRepository.findAll();
		} else {
			ls = roleRepository.queryForList(params);
		}
		return ls.stream().map(this::toTransient).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME_ROLE, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Role update(Long id, Role newEntity) {
		Role source = roleRepository.get(id);
		if (source == null) {
			return null;
		}
		if (hasText(newEntity.getName())) {
			source.setName(newEntity.getName());
		}
		if (newEntity.getRoleType() != null) {
			source.setRoleType(newEntity.getRoleType());
		}
		if (hasText(newEntity.getDescription())) {
			source.setDescription(newEntity.getDescription());
		}
		// 先解除双方关系
		for (Iterator<Authority> i = source.getAuthorities().iterator(); i.hasNext();) {
			Authority a = i.next();
			a.getRoles().remove(source);
			i.remove();
		}
		// 再重新添加
		newEntity.getAuthorities().forEach(a -> {
			Authority p = null;
			if (a.getId() != null) {
				p = authorityRepository.findOne(a.getId());
			} else if (hasText(a.getName())) {
				p = authorityRepository.findByName(a.getName());
			}
			if (p != null) {
				source.getAuthorities().add(p);
				p.getRoles().add(source);
			}
		});
		return toTransient(source);
	}

	@CacheEvict(value = CACHE_NAME_ROLE, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Role source = roleRepository.get(id);
		if (source == null) {
			return;
		}
		// 先解除双方关系
		for (Iterator<Authority> i = source.getAuthorities().iterator(); i.hasNext();) {
			Authority a = i.next();
			a.getRoles().remove(source);
			i.remove();
		}
		for (Iterator<User> i = source.getUsers().iterator(); i.hasNext();) {
			User a = i.next();
			a.getRoles().remove(source);
			i.remove();
		}
		roleRepository.delete(id);
	}

	@Cacheable(value = CACHE_NAME_AUTHORITY)
	@Override
	public List<Authority> getAuthorities() {
		return authorityRepository.findAll().stream().map(this::transientAuthority).collect(Collectors.toList());
	}
	
	@Override
	protected Role toTransient(Role source) {
		if (source == null) {
			return source;
		}
		Role target = new Role();
		BeanUtils.copyProperties(source, target, "users", "authorities");
		source.getAuthorities().forEach(a -> target.getAuthorities().add(new Authority(a.getName(), a.getDescription(), null)));
		return target;
	}

	@Override
	protected Role transientDetail(Role src) {
		if (src == null) {
			return null;
		}
		Role tar = new Role();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getModifyDate());
		Set<Authority> authorities = src.getAuthorities().stream()
				.map(this::transientAuthorityDetail).collect(Collectors.toSet());
		tar.getAuthorities().addAll(authorities);
		return tar;
	}
	
	protected Authority transientAuthority(Authority src) {
		if (src == null) {
			return null;
		}
		Authority tar = new Authority();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getModifyDate());
		return tar;
	}
	
	protected Authority transientAuthorityDetail(Authority src) {
		if (src == null) {
			return null;
		}
		Authority tar = new Authority();
		tar.setId(src.getId());
		tar.setName(src.getName());
		tar.setDescription(src.getDescription());
		tar.setCreateDate(src.getCreateDate());
		tar.setModifyDate(src.getModifyDate());
		tar.setParent(transientAuthorityDetail(src.getParent()));
		return tar;
	}

}
