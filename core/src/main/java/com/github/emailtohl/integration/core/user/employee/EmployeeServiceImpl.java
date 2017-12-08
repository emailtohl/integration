package com.github.emailtohl.integration.core.user.employee;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.emailtohl.integration.common.exception.NotAcceptableException;
import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.fullTextSearch.SearchResult;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.StandardService;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleRepository;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.EmployeeRef;
import com.github.emailtohl.integration.core.user.entities.LoginResult;
import com.github.emailtohl.integration.core.user.org.DepartmentRepository;

/**
 * 平台账号服务层的实现
 * 
 * @author HeLei
 */
@Transactional
@Service
public class EmployeeServiceImpl extends StandardService<Employee> implements EmployeeService {
	private static final transient Logger LOG = LogManager.getLogger();
	private static final transient SecureRandom RANDOM = new SecureRandom();
	private static final transient int HASHING_ROUNDS = 10;
	@Value("${employee.default.password}")
	private String employeeDefaultPassword = "123456";
	@Inject
	EmployeeRepository employeeRepository;
	@Inject
	RoleRepository roleRepository;
	@Inject
	DepartmentRepository departmentRepository;
	@Inject
	EmployeeRefRepository employeeRefRepository;

	/**
	 * 缓存名
	 */
	public static final String CACHE_NAME = "employee_cache";

	@CachePut(value = CACHE_NAME, key = "#result.id")
	@Override
	public Employee create(Employee entity) {
		validate(entity);
		Employee e = new Employee();
		BeanUtils.copyProperties(entity, e, Employee.getIgnoreProperties("employeeRef", "roles", "enabled",
				"credentialsNonExpired", "accountNonLocked", "lastLogin", "lastChangeCredentials", "department"));
		// 关于工号
		synchronized (this) {
			Integer max = employeeRepository.getMaxEmpNo();
			if (max == null) {
				max = Employee.NO1;
			}
			e.setEmpNum(++max);
		}
		// 关于部门
		if (entity.getDepartment() != null && entity.getDepartment().getName() != null) {
			Department d = departmentRepository.findByName(entity.getDepartment().getName());
			if (d != null) {
				e.setDepartment(d);
			}
		}
		// 创建雇员时，可以直接激活可用
		e.setAccountNonLocked(true);
		String pw = e.getPassword();
		if (pw == null || pw.isEmpty()) {
			pw = employeeDefaultPassword;// 设置默认密码
		}
		pw = BCrypt.hashpw(pw, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		e.setPassword(pw);
		Date now = new Date();
		e.setLastLogin(now);
		e.setLastChangeCredentials(now);
		e = employeeRepository.create(e);
		return transientDetail(e);
	}

	@Override
	public boolean exist(Object matcherValue) {
		// 以工号为唯一识别，故始终为false
		return false;
	}

	@Cacheable(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee get(Long id) {
		Employee source = employeeRepository.get(id);
		return transientDetail(source);
	}

	@Override
	public Paging<Employee> query(Employee params, Pageable pageable) {
		Page<Employee> p = employeeRepository.queryForPage(params, pageable);
		List<Employee> content = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
		return new Paging<>(content, pageable, p.getTotalElements());
	}

	@Override
	public List<Employee> query(Employee params) {
		return employeeRepository.queryForList(params).stream().map(this::toTransient).collect(Collectors.toList());
	}

	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee update(Long id, Employee newEntity) {
		validate(newEntity);
		Employee source = employeeRepository.get(id);
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
		if (newEntity.getPost() != null)
			source.setPost(newEntity.getPost());
		if (newEntity.getPublicKey() != null)
			source.setPublicKey(newEntity.getPublicKey());
		if (newEntity.getSalary() != null)
			source.setSalary(newEntity.getSalary());
		if (newEntity.getTelephone() != null)
			source.setTelephone(newEntity.getTelephone());
		if (newEntity.getNickname() != null)
			source.setNickname(newEntity.getNickname());
		// 关于部门
		if (newEntity.getDepartment() != null && newEntity.getDepartment().getName() != null) {
			Department d = departmentRepository.findByName(newEntity.getDepartment().getName());
			if (d != null) {
				source.setDepartment(d);
			}
		}
		return transientDetail(source);
	}

	@CacheEvict(value = CACHE_NAME, key = "#root.args[0]")
	@Override
	public void delete(Long id) {
		Employee source = employeeRepository.getOne(id);
		if (source == null) {
			return;
		}
		if (source.getEmpNum() == null) {
			return;
		}
		if (source.getEmpNum() == Employee.NO1) {
			throw new NotAcceptableException("不能删除系统内置账号");
		}
		// 解除双方关系
		for (Iterator<Role> i = source.getRoles().iterator(); i.hasNext();) {
			Role r = i.next();
			r.getUsers().remove(source);
			i.remove();
		}
		employeeRepository.delete(id);
	}
	
	public Paging<Employee> search(String query, Pageable pageable) {
		if (!StringUtils.hasText(query)) {
			Page<Employee> p = employeeRepository.queryForPage(null, pageable, null);
			List<Employee> ls = p.getContent().stream().map(this::toTransient).collect(Collectors.toList());
			return new Paging<>(ls, p.getTotalElements(), p.getNumber(), p.getSize());
		}
		Page<SearchResult<Employee>> p = employeeRepository.search(query, pageable);
		List<Employee> ls = p.getContent().stream().map(s -> toTransient(s.getEntity())).collect(Collectors.toList());
		return new Paging<>(ls, p.getTotalElements(), p.getNumber(), p.getSize());
	}

	@Override
	public ExecResult login(Integer empNum, String password) {
		Employee source = employeeRepository.findByEmpNum(empNum);
		if (source == null) {
			return new ExecResult(false, LoginResult.notFound.name(), null);
		}
		if (source.getEnabled() != null && !source.getEnabled()) {
			return new ExecResult(false, LoginResult.disabled.name(), null);
		}
		if (source.getAccountNonLocked() != null && !source.getAccountNonLocked()) {
			return new ExecResult(false, LoginResult.locked.name(), null);
		}
		if (source.getAccountNonExpired() != null && !source.getAccountNonExpired()) {
			return new ExecResult(false, LoginResult.accountExpired.name(), null);
		}
		if (source.getCredentialsNonExpired() != null && !source.getCredentialsNonExpired()) {
			return new ExecResult(false, LoginResult.credentialsExpired.name(), null);
		}
		if (!BCrypt.checkpw(password, source.getPassword())) {
			return new ExecResult(false, LoginResult.badCredentials.name(), null);
		}
		source.setLastLogin(new Date());
		return new ExecResult(true, LoginResult.success.name(), transientDetail(source));
	}

	@Override
	public Employee getByEmpNum(Integer empNum) {
		Employee source = employeeRepository.findByEmpNum(empNum);
		return transientDetail(source);
	}

	@Override
	public List<Employee> findByName(String name) {
		return employeeRepository.findByNameLike(name).stream().map(this::toTransient).collect(Collectors.toList());
	}
	
	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee grandRoles(Long id, String... roleNames) {
		Employee source = employeeRepository.get(id);
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

	@Override
	public ExecResult updatePassword(Integer empNum, String oldPassword, String newPassword) {
		Employee source = employeeRepository.findByEmpNum(empNum);
		if (source == null) {
			return new ExecResult(false, "没有此用户", null);
		}
		if (!BCrypt.checkpw(oldPassword, source.getPassword())) {
			return new ExecResult(false, "原密码输入错误", null);
		}
		String hashPw = BCrypt.hashpw(newPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		source.setPassword(hashPw);
		source.setLastChangeCredentials(new Date());
		return new ExecResult(true, "", null);
	}

	@Override
	public ExecResult resetPassword(Long id) {
		Employee source = employeeRepository.get(id);
		if (source == null) {
			return new ExecResult(false, "没有此用户", null);
		}
		String hashPw = BCrypt.hashpw(employeeDefaultPassword, BCrypt.gensalt(HASHING_ROUNDS, RANDOM));
		source.setPassword(hashPw);
		source.setLastChangeCredentials(new Date());
		return new ExecResult(true, "", null);
	}
	
	@CachePut(value = CACHE_NAME, key = "#root.args[0]", condition = "#result != null")
	@Override
	public Employee enabled(Long id, boolean enabled) {
		Employee source = employeeRepository.get(id);
		if (source == null) {
			return null;
		}
		source.setEnabled(enabled);
		if (enabled) {// 同时解锁
			source.setAccountNonLocked(true);
		}
		return transientDetail(source);
	}
	
	@Value("${account.expire.month}")
	Integer accountExpireMonth;
	@Value("${credentials.expire.month}")
	Integer credentialsExpireMonth;
	/**
	 * 若没有配置，则表示没有过期时间
	 */
	@PostConstruct
	public void init() {
		if (accountExpireMonth == null) {
			accountExpireMonth = Integer.MAX_VALUE;
		}
		if (credentialsExpireMonth == null) {
			credentialsExpireMonth = Integer.MAX_VALUE;
		}
	}
	
	@Override
	protected Employee toTransient(Employee source) {
		if (source == null) {
			return null;
		}
		Employee target = new Employee();
		BeanUtils.copyProperties(source, target, "employeeRef", "password", "roles");
		return target;
	}
	
	@Override
	protected Employee transientDetail(Employee source) {
		if (source == null) {
			return null;
		}
		Employee target = new Employee();
		BeanUtils.copyProperties(source, target, "employeeRef", "password", "roles");
		source.getRoles().forEach(role -> target.getRoles().add(new Role(role.getName(), role.getDescription())));
		return target;
	}

	@Override
	public EmployeeRef getRef(Long id) {
		EmployeeRef ref = employeeRefRepository.findOne(id);
		if (ref == null) {
			return null;
		}
		return toTransientRef(ref);
	}

	@Override
	public EmployeeRef findRefByEmpNum(Integer empNum) {
		EmployeeRef ref = employeeRefRepository.findByEmpNum(empNum);
		if (ref == null) {
			return null;
		}
		return toTransientRef(ref);
	}

	/**
	 * 引用实体匹配器
	 */
	private ExampleMatcher refMatcher = ExampleMatcher.matching()
			.withIgnoreNullValues()
			.withIgnorePaths("icon", "employee")
			.withMatcher("id", GenericPropertyMatchers.exact())
			.withMatcher("empNum", GenericPropertyMatchers.exact())
			.withMatcher("name", GenericPropertyMatchers.caseSensitive())
			.withMatcher("nickname", GenericPropertyMatchers.caseSensitive())
			.withMatcher("email", GenericPropertyMatchers.caseSensitive())
			.withMatcher("nickname", GenericPropertyMatchers.caseSensitive())
			.withMatcher("cellPhone", GenericPropertyMatchers.caseSensitive());
	
	@Override
	public Paging<EmployeeRef> queryRef(EmployeeRef params, Pageable pageable) {
		Page<EmployeeRef> page;
		if (params == null) {
			page = employeeRefRepository.findAll(pageable);
		} else {
			Example<EmployeeRef> example = Example.of(params, refMatcher);
			page = employeeRefRepository.findAll(example, pageable);
		}
		List<EmployeeRef> ls = page.getContent().stream().map(this::toTransientRef).collect(Collectors.toList());
		return new Paging<EmployeeRef>(ls, pageable, page.getTotalElements());
	}

	@Override
	public List<EmployeeRef> queryRef(EmployeeRef params) {
		List<EmployeeRef> ls;
		if (params == null) {
			ls = employeeRefRepository.findAll();
		} else {
			Example<EmployeeRef> example = Example.of(params, refMatcher);
			ls = employeeRefRepository.findAll(example);
		}
		return ls.stream().map(this::toTransientRef).collect(Collectors.toList());
	}
	
	private EmployeeRef toTransientRef(EmployeeRef ref) {
		EmployeeRef copy = new EmployeeRef();
		copy.setId(ref.getId());
		copy.setEmpNum(ref.getEmpNum());
		copy.setCellPhone(ref.getCellPhone());
		copy.setEmail(ref.getEmail());
		copy.setName(ref.getName());
		copy.setNickname(ref.getNickname());
		copy.setIcon(ref.getIcon());
		return copy;
	}
	
	@Override
	public void accountStatus() {
		final LocalDate today = LocalDate.now();
		employeeRepository.findAll().stream()
		.filter(u -> !u.getEmpNum().equals(Employee.NO1))
		// 最后登录时间的维护
		.peek(u -> {
			Date d = u.getLastLogin();
			if (d == null) {
				LOG.debug("lastLoginTime: null {} accountNonExpired : false", u.getId());
				u.setAccountNonExpired(false);
				return;
			}
			Instant instant = d.toInstant();
			ZoneId zoneId = ZoneId.systemDefault();
			LocalDate lastLogin = instant.atZone(zoneId).toLocalDate();
			// 过期了
			if (today.minusMonths(accountExpireMonth).isAfter(lastLogin)) {
				LOG.debug( "today: {} lastLogin: {} {} accountNonExpired : false", today, lastLogin, u.getId());
				u.setAccountNonExpired(false);
			}
		})
		// 密码过期的维护
		.peek(u ->  {
			Date d = u.getLastChangeCredentials();
			if (d == null) {
				LOG.debug("lastChangeCredentials: null {} credentialsNonExpired : false", u.getId());
				u.setCredentialsNonExpired(false);
				return;
			}
			Instant instant = d.toInstant();
			ZoneId zoneId = ZoneId.systemDefault();
			LocalDate lastChangeCredentials = instant.atZone(zoneId).toLocalDate();
			if (today.minusMonths(credentialsExpireMonth).isAfter(lastChangeCredentials)) {
				LOG.debug( "today: {} lastChangeCredentials: {}  {} credentialsNonExpired : false", today, lastChangeCredentials, u.getId());
				u.setCredentialsNonExpired(false);
			}
		});
	}
}
