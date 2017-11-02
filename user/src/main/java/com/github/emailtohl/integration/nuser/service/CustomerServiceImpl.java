package com.github.emailtohl.integration.nuser.service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.Constant;
import com.github.emailtohl.integration.common.exception.InvalidDataException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.entity.BaseEntity;
import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.nuser.dao.CustomerRepository;
import com.github.emailtohl.integration.nuser.dao.RoleRepository;
import com.github.emailtohl.integration.nuser.entities.Card;
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.Customer.Level;
import com.github.emailtohl.integration.nuser.entities.Role;

/**
 * 外部人员的服务接口
 * 
 * @author HeLei
 *
 */
@Transactional
@Service
public class CustomerServiceImpl implements CustomerService {
	public static final Pattern EMAIL_PATTERN = Pattern.compile(Constant.PATTERN_EMAIL);
	private static final transient SecureRandom RANDOM = new SecureRandom();
	private static final transient int HASHING_ROUNDS = 10;
	private static final transient ConcurrentHashMap<String, String> TOKEN_MAP = new ConcurrentHashMap<String, String>();
	@Value("${customer.default.password}")
	private String customerDefaultPassword;
	@Value("${token.expire}")
	private int tokenExpire;
	@Inject
	CustomerRepository customerRepository;
	@Inject
	RoleRepository roleRepository;
	@Inject
	ThreadPoolTaskScheduler taskScheduler;

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
		if (!StringUtils.hasText(entity.getCellPhone()) && !StringUtils.hasText(entity.getEmail())) {
			throw new InvalidDataException("注册时既未填入手机号也未填入邮箱地址，不能注册");
		}
		Customer c = new Customer();
		BeanUtils.copyProperties(entity, c,
				BaseEntity.getIgnoreProperties("roles", "accountNonLocked", "level", "cards"));
		c.setAccountNonLocked(true);
		c.setLevel(Level.ORDINARY);
		String pw = c.getPassword();
		if (pw == null || pw.isEmpty()) {
			throw new InvalidDataException("请输入密码");
		}
		pw = BCrypt.hashpw(pw, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		c.setPassword(pw);
		c = customerRepository.save(c);
		return transientDetail(c);
	}

	@Override
	public boolean exist(String propertyName, Object matcherValue) {
		String _matcherValue = (String) matcherValue;
		Customer c = new Customer();
		Example<Customer> example;
		Matcher m = EMAIL_PATTERN.matcher(_matcherValue);
		if (m.find()) {
			c.setEmail(_matcherValue);
			example = Example.<Customer>of(c, emailMatcher);
		} else {
			c.setCellPhone(_matcherValue);
			example = Example.<Customer>of(c, cellPhoneMatcher);
		}
		return customerRepository.exists(example);
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer get(Long id) {
		Customer source = customerRepository.get(id);
		return transientDetail(source);
	}

	@Override
	public Paging<Customer> query(Customer params, Pageable pageable) {
		Page<Customer> p = customerRepository.queryForPage(params, pageable);
		List<Customer> content = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(content, pageable, p.getTotalElements());
	}

	@Override
	public List<Customer> query(Customer params) {
		return customerRepository.queryForList(params).stream().map(this::toTransient).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer update(Long id, Customer newEntity) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		if (newEntity.getBirthday() != null)
			source.setBirthday(newEntity.getBirthday());
		if (newEntity.getDescription() != null)
			source.setDescription(newEntity.getDescription());
		if (newEntity.getEmail() != null)
			source.setEmail(newEntity.getEmail());
		if (newEntity.getGender() != null)
			source.setGender(newEntity.getGender());
		if (newEntity.getImage() != null)
			source.setImage(newEntity.getImage());
		if (newEntity.getName() != null)
			source.setName(newEntity.getName());
		if (newEntity.getPublicKey() != null)
			source.setPublicKey(newEntity.getPublicKey());
		if (newEntity.getTelephone() != null)
			source.setTelephone(newEntity.getTelephone());
		if (newEntity.getNickname() != null)
			source.setNickname(newEntity.getNickname());
		if (newEntity.getIdentification() != null)
			source.setIdentification(newEntity.getIdentification());
		if (newEntity.getAddress() != null)
			source.setAddress(newEntity.getAddress());
		return transientDetail(source);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Customer source = customerRepository.getOne(id);
		// 解除双方关系
		for (Iterator<Role> i = source.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(source);
			i.remove();
		}
		customerRepository.delete(id);
	}

	@Override
	public ExecResult login(String cellPhoneOrEmail, String password) {
		Customer c = find(cellPhoneOrEmail);
		if (c == null) {
			return new ExecResult(false, LoginResult.notFound.name(), null);
		}
		if (!BCrypt.checkpw(password, c.getPassword())) {
			return new ExecResult(false, LoginResult.badCredentials.name(), null);
		}
		c.setLastLoginTime(new Date());
		return new ExecResult(true, LoginResult.success.name(), transientDetail(c));
	}

	@Override
	public Customer findByCellPhoneOrEmail(String cellPhoneOrEmail) {
		return transientDetail(find(cellPhoneOrEmail));
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer grandRoles(Long id, String... roleNames) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		// 解除双方关系
		for (Iterator<Role> i = source.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(source);
			i.remove();
		}
		for (String name : roleNames) {
			Role r = roleRepository.findByName(name);
			if (r != null) {
				source.getRoles().add(r);
				r.getUsers().add(source);
			}
		}
		return transientDetail(source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer grandLevel(Long id, Level level) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		source.setLevel(level);
		return transientDetail(source);
	}

	@Override
	public String getToken(String cellPhoneOrEmail) {
		final String key = UUID.randomUUID().toString();
		TOKEN_MAP.put(key, cellPhoneOrEmail);
		taskScheduler.schedule(() -> TOKEN_MAP.remove(key), new Date(System.currentTimeMillis() + tokenExpire));
		return key;
	}

	@Override
	public ExecResult updatePassword(String cellPhoneOrEmail, String newPassword, String token) {
		if (token == null) {
			return new ExecResult(false, "没有token", null);
		}
		String s = TOKEN_MAP.get(token);
		if (s == null) {
			return new ExecResult(false, "token无效或过期", null);
		}
		Customer source = find(cellPhoneOrEmail);
		if (source == null) {
			return new ExecResult(false, "没有此用户:" + cellPhoneOrEmail, null);
		}
		if (!s.equals(source.getCellPhone()) && !s.equals(source.getEmail())) {
			return new ExecResult(false, "token无效或过期", null);
		}
		String hashPw = BCrypt.hashpw(newPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		source.setPassword(hashPw);
		return new ExecResult(true, "", null);
	}

	@Override
	public ExecResult resetPassword(Long id) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return new ExecResult(false, "没有此用户", null);
		}
		String hashPw = BCrypt.hashpw(customerDefaultPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		source.setPassword(hashPw);
		return new ExecResult(true, "", null);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer changeCellPhone(Long id, String newCellPhone) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		source.setCellPhone(newCellPhone);
		return transientDetail(source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer changeEmail(Long id, String newEmail) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		source.setEmail(newEmail);
		return transientDetail(source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer lock(Long id, boolean lock) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		source.setAccountNonLocked(!lock);
		return transientDetail(source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer updateCards(Long id, Set<Card> cards) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		source.getCards().clear();
		source.getCards().addAll(cards);
		return transientDetail(source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer addCard(Long id, Card card) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		source.getCards().add(card);
		return transientDetail(source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer removeCard(Long id, Card card) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		source.getCards().remove(card);
		return transientDetail(source);
	}

	/**
	 * @param cellPhoneOrEmail
	 * @return 持久化的Customer
	 */
	private Customer find(String cellPhoneOrEmail) {
		Matcher m = EMAIL_PATTERN.matcher(cellPhoneOrEmail);
		Customer c;
		if (m.find()) {
			c = customerRepository.findByEmail(cellPhoneOrEmail);
		} else {
			c = customerRepository.findByCellPhone(cellPhoneOrEmail);
		}
		return c;
	}

	private Customer toTransient(Customer source) {
		if (source == null) {
			return null;
		}
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, "password", "roles", "cards");
		return target;
	}

	private Customer transientDetail(Customer source) {
		if (source == null) {
			return null;
		}
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, "password", "roles", "cards");
		source.getCards().forEach(card -> target.getCards().add(card));
		source.getRoles().forEach(role -> target.getRoles().add(new Role(role.getName(), role.getDescription())));
		return target;
	}

}