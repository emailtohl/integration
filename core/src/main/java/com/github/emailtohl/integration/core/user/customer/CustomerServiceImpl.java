package com.github.emailtohl.integration.core.user.customer;

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

import com.github.emailtohl.integration.common.ConstantPattern;
import com.github.emailtohl.integration.common.exception.InvalidDataException;
import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchResult;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleRepository;
import com.github.emailtohl.integration.core.user.Constant;
import com.github.emailtohl.integration.core.user.entities.Card;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Customer.Level;
import com.github.emailtohl.integration.core.user.entities.CustomerRef;
import com.github.emailtohl.integration.core.user.entities.LoginResult;

/**
 * 客户的服务接口
 * 
 * @author HeLei
 */
@Transactional
@Service
public class CustomerServiceImpl implements CustomerService {
	public static final Pattern PATTERN_CELL_PHONE = Pattern.compile(ConstantPattern.CELL_PHONE);
	public static final Pattern EMAIL_PATTERN = Pattern.compile(ConstantPattern.EMAIL);
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
	CustomerRefRepository customerRefRepository;
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
	public Customer create(Customer entity) {
		if (!StringUtils.hasText(entity.getCellPhone()) && !StringUtils.hasText(entity.getEmail())) {
			throw new InvalidDataException("注册时既未填入手机号也未填入邮箱地址，不能注册");
		}
		Customer c = new Customer();
		BeanUtils.copyProperties(entity, c, Customer.getIgnoreProperties("customerRef", "roles", "enabled",
				"credentialsNonExpired", "accountNonLocked", "lastLogin", "lastChangeCredentials", "level", "cards"));
		c.setAccountNonLocked(true);
		c.setLevel(Level.ORDINARY);
		String pw = c.getPassword();
		if (pw == null || pw.isEmpty()) {
			throw new InvalidDataException("请输入密码");
		}
		pw = BCrypt.hashpw(pw, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		c.setPassword(pw);
		Date now = new Date();
		c.setLastLogin(now);
		c.setLastChangeCredentials(now);
		c = customerRepository.create(c);
		return transientDetail(c);
	}

	@Override
	public boolean exist(Object matcherValue) {
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
		Page<Customer> p;
		if (params == null) {
			p = customerRepository.findAll(pageable);
		} else {
			p = customerRepository.queryForPage(params, pageable);
		}
		List<Customer> content = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(content, pageable, p.getTotalElements());
	}

	@Override
	public List<Customer> query(Customer params) {
		List<Customer> ls;
		if (params == null) {
			ls = customerRepository.findAll();
		} else {
			ls = customerRepository.queryForList(params);
		}
		return ls.stream().map(this::toTransient).collect(Collectors.toList());
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
		if (Constant.ANONYMOUS_NAME.equals(source.getName())) {
			throw new NotAcceptableException("不能删除系统内置账号");
		}
		// 解除双方关系
		for (Iterator<Role> i = source.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(source);
			i.remove();
		}
		customerRepository.delete(id);
	}

	@Override
	public Paging<Customer> search(String query, Pageable pageable) {
		if (!StringUtils.hasText(query)) {
			Page<Customer> p = customerRepository.queryForPage(null, pageable, null);
			List<Customer> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
			return new Paging<>(ls, p.getTotalElements(), p.getNumber(), p.getSize());
		}
		Page<SearchResult<Customer>> p = customerRepository.search(query, pageable);
		List<Customer> ls = p.getContent().stream().map(s -> toTransient(s.getEntity())).collect(Collectors.toList());
		return new Paging<>(ls, p.getTotalElements(), p.getNumber(), p.getSize());
	}
	
	@Override
	public ExecResult login(String cellPhoneOrEmail, String password) {
		Customer c = find(cellPhoneOrEmail);
		if (c == null) {
			return new ExecResult(false, LoginResult.notFound.name(), null);
		}
		if (c.getEnabled() != null && !c.getEnabled()) {
			return new ExecResult(false, LoginResult.disabled.name(), null);
		}
		if (c.getAccountNonLocked() != null && !c.getAccountNonLocked()) {
			return new ExecResult(false, LoginResult.locked.name(), null);
		}
		if (c.getAccountNonExpired() != null && !c.getAccountNonExpired()) {
			return new ExecResult(false, LoginResult.accountExpired.name(), null);
		}
		if (c.getCredentialsNonExpired() != null && !c.getCredentialsNonExpired()) {
			return new ExecResult(false, LoginResult.credentialsExpired.name(), null);
		}
		if (!BCrypt.checkpw(password, c.getPassword())) {
			return new ExecResult(false, LoginResult.badCredentials.name(), null);
		}
		c.setLastLogin(new Date());
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
		source.setLastChangeCredentials(new Date());
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
		source.setLastChangeCredentials(new Date());
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
	public Customer enabled(Long id, boolean enabled) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		source.setEnabled(enabled);
		if (enabled) {// 同时解锁
			source.setAccountNonLocked(true);
		}
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
		if (cellPhoneOrEmail == null) {
			return null;
		}
		Customer c = null;
		Matcher m = PATTERN_CELL_PHONE.matcher(cellPhoneOrEmail);
		if (m.find()) {
			c = customerRepository.findByCellPhone(cellPhoneOrEmail);
		}
		if (c == null) {
			m = EMAIL_PATTERN.matcher(cellPhoneOrEmail);
			if (m.find()) {
				c = customerRepository.findByEmail(cellPhoneOrEmail);
			}
		}
		return c;
	}

	private Customer toTransient(Customer source) {
		if (source == null) {
			return null;
		}
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, "customerRef", "password", "roles", "cards");
		return target;
	}

	private Customer transientDetail(Customer source) {
		if (source == null) {
			return null;
		}
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, "customerRef", "password", "roles", "cards");
		source.getCards().forEach(card -> target.getCards().add(card));
		source.getRoles().forEach(role -> target.getRoles().add(new Role(role.getName(), role.getDescription())));
		return target;
	}

	@Override
	public CustomerRef getRef(Long id) {
		CustomerRef ref = customerRefRepository.findOne(id);
		if (ref == null) {
			return null;
		}
		return toTransientRef(ref);
	}

	@Override
	public CustomerRef findRefByCellPhoneOrEmail(String cellPhoneOrEmail) {
		if (cellPhoneOrEmail == null) {
			return null;
		}
		CustomerRef ref = null;
		Matcher m = PATTERN_CELL_PHONE.matcher(cellPhoneOrEmail);
		if (m.find()) {
			ref = customerRefRepository.findByCellPhone(cellPhoneOrEmail);
		}
		if (ref == null) {
			m = EMAIL_PATTERN.matcher(cellPhoneOrEmail);
			if (m.find()) {
				ref = customerRefRepository.findByEmail(cellPhoneOrEmail);
			}
		}
		return toTransientRef(ref);
	}

	/**
	 * 引用实体匹配器
	 */
	private ExampleMatcher refMatcher = ExampleMatcher.matching()
			.withIgnoreNullValues()
			.withIgnorePaths("icon", "customer")
			.withMatcher("id", GenericPropertyMatchers.exact())
			.withMatcher("name", GenericPropertyMatchers.caseSensitive())
			.withMatcher("nickname", GenericPropertyMatchers.caseSensitive())
			.withMatcher("email", GenericPropertyMatchers.caseSensitive())
			.withMatcher("nickname", GenericPropertyMatchers.caseSensitive())
			.withMatcher("cellPhone", GenericPropertyMatchers.caseSensitive());
	
	@Override
	public Paging<CustomerRef> queryRef(CustomerRef params, Pageable pageable) {
		Page<CustomerRef> page;
		if (params == null) {
			page = customerRefRepository.findAll(pageable);
		} else {
			Example<CustomerRef> example = Example.of(params, refMatcher);
			page = customerRefRepository.findAll(example, pageable);
		}
		List<CustomerRef> ls = page.getContent().stream().map(this::toTransientRef).collect(Collectors.toList());
		return new Paging<CustomerRef>(ls, pageable, page.getTotalElements());
	}

	@Override
	public List<CustomerRef> queryRef(CustomerRef params) {
		List<CustomerRef> ls;
		if (params == null) {
			ls = customerRefRepository.findAll();
		} else {
			Example<CustomerRef> example = Example.of(params, refMatcher);
			ls = customerRefRepository.findAll(example);
		}
		return ls.stream().map(this::toTransientRef).collect(Collectors.toList());
	}

	private CustomerRef toTransientRef(CustomerRef ref) {
		CustomerRef copy = new CustomerRef();
		copy.setId(ref.getId());
		copy.setCellPhone(ref.getCellPhone());
		copy.setEmail(ref.getEmail());
		copy.setName(ref.getName());
		copy.setNickname(ref.getNickname());
		copy.setIcon(ref.getIcon());
		return copy;
	}
}
