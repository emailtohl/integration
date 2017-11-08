package com.github.emailtohl.integration.nuser.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchResult;
import com.github.emailtohl.integration.nuser.dao.UserRepository;
import com.github.emailtohl.integration.nuser.entities.User;

/**
 * 统一查询功能
 * @author HeLei
 */
@Service
public class UserServiceImpl implements UserService {
	private static final transient Logger LOG = LogManager.getLogger();
	
	@Value("${account.expire.month}")
	int accountExpireMonth;
	@Value("${credentials.expire.month}")
	int credentialsExpireMonth;
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

	@Override
	public void accountStatus() {
		LocalDate today = LocalDate.now();
		userRepository.findAll().stream().forEach(u -> {
			Date d = u.getLastLoginTime();
			if (d == null) {
				LOG.debug("lastLoginTime: null {} accountNonExpired : false", u.getId());
				u.setAccountNonExpired(false);
				u.setCredentialsNonExpired(false);
				return;
			}
			Instant instant = d.toInstant();
			ZoneId zoneId = ZoneId.systemDefault();
			LocalDate lastLoginTime = instant.atZone(zoneId).toLocalDate();
			// 过期了
			if (today.minusMonths(accountExpireMonth).isAfter(lastLoginTime)) {
				LOG.debug( "today: {} lastLoginTime: {} {} accountNonExpired : false", today, lastLoginTime, u.getId());
				u.setAccountNonExpired(false);
			}
			if (today.minusMonths(credentialsExpireMonth).isAfter(lastLoginTime)) {
				LOG.debug( "today: {} lastLoginTime: {}  {} credentialsNonExpired : false", today, lastLoginTime, u.getId());
				u.setCredentialsNonExpired(false);
			}
		});
	}

}
