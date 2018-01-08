package com.github.emailtohl.integration.core.user.customer;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import org.springframework.stereotype.Service;

import com.github.emailtohl.integration.common.exception.InvalidDataException;
import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleRepository;
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
public class CustomerServiceImpl extends StandardService<Customer> implements CustomerService {
	private static final transient ConcurrentHashMap<String, String> TOKEN_MAP = new ConcurrentHashMap<String, String>();
	@Value("${" + Constant.PROP_CUSTOMER_DEFAULT_PASSWORD + "}")
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
	 * 缓存名
	 */
	public static final String CACHE_NAME = "customer_cache";

	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Customer create(Customer entity) {
		validate(entity);
		if (!hasText(entity.getCellPhone()) && !hasText(entity.getEmail())) {
			throw new InvalidDataException("注册时既未填入手机号也未填入邮箱地址，不能注册");
		}
		Customer c = new Customer();
		BeanUtils.copyProperties(entity, c, Customer.getIgnoreProperties("customerRef", "roles", "enabled",
				"credentialsNonExpired", "accountNonLocked", "lastLogin", "lastChangeCredentials", "level", "cards"));
		c.setAccountNonLocked(true);
		c.setLevel(Level.ORDINARY);
		String pw = c.getPassword();
		if (hasText(pw)) {
			pw = hashpw(pw);
		} else {
			pw = hashpw(customerDefaultPassword);
		}
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
		return customerRepository.usernameIsExist(_matcherValue);
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
		validate(newEntity);
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		isIllegal(source);
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
		if (source == null) {
			return;
		}
		isIllegal(source);
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
		if (!hasText(query)) {
			Page<Customer> p = customerRepository.queryForPage(null, pageable, null);
			List<Customer> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
			return new Paging<>(ls, p.getTotalElements(), p.getNumber(), p.getSize());
		}
		Page<Customer> p = customerRepository.search(query, pageable);
		List<Customer> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(ls, p.getTotalElements(), p.getNumber(), p.getSize());
	}
	
	@Override
	public ExecResult login(String username, String password) {
		if (!hasText(username) || !hasText(password)) {
			return new ExecResult(false, LoginResult.notFound.name(), null);
		}
		Customer c = customerRepository.findByUsername(username);
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
		if (!checkpw(password, c.getPassword())) {
			return new ExecResult(false, LoginResult.badCredentials.name(), null);
		}
		c.setLastLogin(new Date());
		return new ExecResult(true, LoginResult.success.name(), transientDetail(c));
	}

	@Override
	public Customer findByUsername(String username) {
		return transientDetail(customerRepository.findByUsername(username));
	}

	@Override
	public List<String> getUsernames(Long id) {
		return customerRepository.getUsernames(id);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer grandRoles(Long id, String... roleNames) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		isIllegal(source);
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
		isIllegal(source);
		source.setLevel(level);
		return transientDetail(source);
	}

	@Override
	public String getToken(String username) {
		final String key = UUID.randomUUID().toString();
		TOKEN_MAP.put(key, username);
		taskScheduler.schedule(() -> TOKEN_MAP.remove(key), new Date(System.currentTimeMillis() + tokenExpire));
		return key;
	}

	@Override
	public ExecResult updatePassword(String username, String newPassword, String token) {
		if (token == null) {
			return new ExecResult(false, "没有token", null);
		}
		String s = TOKEN_MAP.get(token);
		if (s == null) {
			return new ExecResult(false, "token无效或过期", null);
		}
		Customer source = customerRepository.findByUsername(username);
		if (source == null) {
			return new ExecResult(false, "没有此用户:" + username, null);
		}
		if (!s.equals(source.getCellPhone()) && !s.equals(source.getEmail())) {
			return new ExecResult(false, "token无效或过期", null);
		}
		String hashPw = hashpw(newPassword);
		source.setPassword(hashPw);
		source.setLastChangeCredentials(new Date());
		return new ExecResult(true, "", source);
	}

	@Override
	public ExecResult resetPassword(Long id) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return new ExecResult(false, "没有此用户", null);
		}
		String hashPw = hashpw(customerDefaultPassword);
		source.setPassword(hashPw);
		source.setLastChangeCredentials(new Date());
		return new ExecResult(true, "", source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer changeCellPhone(Long id, String newCellPhone) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		isIllegal(source);
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
		isIllegal(source);
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
		isIllegal(source);
		source.setEnabled(enabled);
		if (enabled) {// 同时解锁
			source.setAccountNonLocked(true);
		}
		return transientDetail(source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer addCard(Long id, Card card) {
		validate(card, Card.class);
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		isIllegal(source);
		source.getCards().add(card);
		return transientDetail(source);
	}
	
	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer updateCards(Long id, Set<Card> cards) {
		cards.forEach(c -> validate(c, Card.class));
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		isIllegal(source);
		source.getCards().clear();
		source.getCards().addAll(cards);
		return transientDetail(source);
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Customer removeCard(Long id, Card card) {
		Customer source = customerRepository.get(id);
		if (source == null) {
			return null;
		}
		isIllegal(source);
		source.getCards().remove(card);
		return transientDetail(source);
	}

	@Override
	protected Customer toTransient(Customer source) {
		if (source == null) {
			return null;
		}
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, "usernames", "customerRef", "password", "roles", "cards");
		return target;
	}

	@Override
	protected Customer transientDetail(Customer source) {
		if (source == null) {
			return null;
		}
		Customer target = new Customer();
		BeanUtils.copyProperties(source, target, "usernames", "customerRef", "password", "roles", "cards");
		source.getCards().forEach(card -> target.getCards().add(card));
		source.getRoles().forEach(role -> target.getRoles().add(new Role(role.getName(), role.getRoleType(), role.getDescription())));
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
	public CustomerRef findRefByUsername(String username) {
		if (!hasText(username)) {
			return null;
		}
		return toTransientRef(customerRepository.findRefByUsername(username));
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
		if (ref == null) {
			return null;
		}
		CustomerRef copy = new CustomerRef();
		copy.setId(ref.getId());
		copy.setCellPhone(ref.getCellPhone());
		copy.setEmail(ref.getEmail());
		copy.setName(ref.getName());
		copy.setNickname(ref.getNickname());
		copy.setIconSrc(ref.getIconSrc());
		return copy;
	}
	
	/**
	 * 若是系统内置账号，则触发异常
	 * @param e
	 */
	private void isIllegal(Customer c) {
		if (Constant.ANONYMOUS_EMAIL.equals(c.getEmail())) {
			throw new NotAcceptableException("不能删除系统内置账号");
		}
	}
}
