package com.github.emailtohl.integration.core.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchResult;
import com.github.emailtohl.integration.core.user.dao.UserRepository;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 统一查询功能
 * @author HeLei
 */
@Service
public class UserServiceImpl implements UserService {
	@Inject
	UserRepository userRepository;

	@Override
	public Paging<User> search(String fulltext, Pageable pageable) {
		Page<SearchResult<User>> p = userRepository.search(fulltext, pageable);
		List<User> ls = new ArrayList<>();
		p.forEach(r -> ls.add(toTransient(r.getEntity())));
		return new Paging<>(ls, pageable, p.getTotalElements());
	}
	
	@Override
	public Paging<User> query(User params, Pageable pageable) {
		Page<User> p = userRepository.queryForPage(params, pageable);
		List<User> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, pageable, p.getTotalElements());
	}

	@Override
	public User get(Long id) {
		User source = userRepository.get(id);
		return transientDetail(source);
	}
	
	private User toTransient(User source) {
		if (source == null) {
			return null;
		}
		User target = new User();
		BeanUtils.copyProperties(source, target, "password", "roles");
		return target;
	}
	
	private User transientDetail(User source) {
		if (source == null) {
			return null;
		}
		User target = new User();
		BeanUtils.copyProperties(source, target, "password", "roles", "cards");
		return target;
	}

}
