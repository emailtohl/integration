package com.github.emailtohl.integration.nuser.service;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.Constant;
import com.github.emailtohl.integration.common.exception.InvalidDataException;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.nuser.dao.CustomerRepository;
import com.github.emailtohl.integration.nuser.dao.RoleRepository;
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.Customer.Level;
import com.github.emailtohl.integration.nuser.entities.Role;

/**
 * 外部人员的服务接口
 * @author HeLei
 *
 */
@Transactional
@Service
public class CustomerServiceImpl implements CustomerService {
	public static final Pattern EMAIL_PATTERN = Pattern.compile(Constant.PATTERN_EMAIL);
	private static final transient SecureRandom RANDOM = new SecureRandom();
	private static final transient int HASHING_ROUNDS = 10;
	private static final String DEFAULT_PASSWORD = "123456";
	@Inject
	CustomerRepository customerRepository;
	@Inject
	RoleRepository roleRepository;
	
	 /**
     * 用于匹配的邮箱的matcher
     */
	private ExampleMatcher emailMatcher = ExampleMatcher.matching().withMatcher("email",
			GenericPropertyMatchers.caseSensitive());
	/**
	 * 用于匹配的电话的matcher
	 */
	private ExampleMatcher cellPhoneMatcher = ExampleMatcher.matching().withMatcher("cellPhone",
			GenericPropertyMatchers.caseSensitive());

	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "customer_cache";
	
	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Customer create(@Valid Customer entity) {
		Customer c = new Customer();
		BeanUtils.copyProperties(entity, c, BaseEntity.getIgnoreProperties("roles", "accountNonLocked", "level", "cards"));
		c.setAccountNonLocked(true);
		c.setLevel(Level.ORDINARY);
		String pw = c.getPassword();
		if (pw == null || pw.isEmpty()) {
			throw new InvalidDataException("请输入密码");
		}
		pw = BCrypt.hashpw(pw, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		c.setPassword(pw);
		c = customerRepository.save(c);
		return filter(c);
	}

	@Override
	public boolean exist(String telOrEmail, Object matcherValue) {
		Customer c = new Customer();
		Example<Customer> example;
		Matcher m = EMAIL_PATTERN.matcher(telOrEmail);
		if (m.find()) {
			c.setEmail(telOrEmail);
			example = Example.<Customer> of(c, emailMatcher);
		} else {
			c.setCellPhone(telOrEmail);
			example = Example.<Customer> of(c, cellPhoneMatcher);
		}
		return customerRepository.exists(example);
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer get(Long id) {
		Customer c = customerRepository.findOne(id);
		return filter(c);
	}

	@Override
	public Page<Customer> query(Customer params, Pageable pageable) {
		Page<Customer> p = customerRepository.queryForPage(params, pageable);
		List<Customer> content = p.getContent().stream().map(this::filter).collect(Collectors.toList());
		return new PageImpl<>(content, pageable, p.getTotalElements());
	}

	@Override
	public List<Customer> query(Customer params) {
		return customerRepository.queryForList(params).stream().map(this::filter).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer update(Long id, Customer newEntity) {
		Customer target = customerRepository.findOne(id);
		if (target == null) {
			return null;
		}
		target.setBirthday(newEntity.getBirthday());
		target.setDescription(newEntity.getDescription());
		target.setEmail(newEntity.getEmail());
		target.setGender(newEntity.getGender());
		target.setImage(newEntity.getImage());
		target.setName(newEntity.getName());
		target.setPublicKey(newEntity.getPublicKey());
		target.setTelephone(newEntity.getTelephone());
		target.setNickname(newEntity.getNickname());
		target.setIdentification(newEntity.getIdentification());
		target.setAddress(newEntity.getAddress());
		return filter(target);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Customer target = customerRepository.getOne(id);
		// 解除双方关系
		for (Iterator<Role> i = target.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(target);
			i.remove();
		}
		customerRepository.delete(id);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer grandRoles(Long id, String... roleNames) {
		Customer target = customerRepository.findOne(id);
		if (target == null) {
			return null;
		}
		// 解除双方关系
		for (Iterator<Role> i = target.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(target);
			i.remove();
		}
		for (String name : roleNames) {
			Role r = roleRepository.findByName(name);
			if (r != null) {
				target.getRoles().add(r);
				r.getUsers().add(target);
			}
		}
		return filter(target);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer grandLevel(Long id, Level level) {
		Customer target = customerRepository.findOne(id);
		if (target == null) {
			return null;
		}
		target.setLevel(level);
		return filter(target);
	}

	@Override
	public ExecResult updatePassword(Long id, String newPassword) {
		Customer target = customerRepository.findOne(id);
		if (target == null) {
			return new ExecResult(false, "没有此用户");
		}
		String hashPw = BCrypt.hashpw(newPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		target.setPassword(hashPw);
		return new ExecResult(true, "");
	}
	
	@Override
	public ExecResult resetPassword(Long id) {
		Customer target = customerRepository.findOne(id);
		if (target == null) {
			return new ExecResult(false, "没有此用户");
		}
		String hashPw = BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		target.setPassword(hashPw);
		return new ExecResult(true, "");
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer lock(Long id, boolean lock) {
		Customer target = customerRepository.findOne(id);
		if (target == null) {
			return null;
		}
		target.setAccountNonLocked(lock);
		return filter(target);
	}

	private Customer filter(Customer source) {
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, Customer.getIgnoreProperties("password"));
		target.setId(source.getId());
		return target;
	}
}
