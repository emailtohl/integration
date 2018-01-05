package com.github.emailtohl.integration.web;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.user.customer.CustomerRepository;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeRepository;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 用户和角色接口依赖core的数据源，要使用它们需要mock
 * 
 * @author HeLei
 */
@Configurable
@PropertySource({ "classpath:config.properties" })
@EnableCaching
@EnableTransactionManagement
public class UserRoleConfig {
	private AtomicLong id = new AtomicLong(1L);
	private Map<Long, Role> roleDB = new ConcurrentHashMap<>();
	private Map<String, Role> roleNameDB = new ConcurrentHashMap<>();
	private Map<Long, com.github.emailtohl.integration.core.user.entities.User> userDB = new ConcurrentHashMap<>();
	
	@Value("${" + Constant.PROP_CUSTOMER_DEFAULT_PASSWORD + "}")
	private String customerDefaultPassword;
	@Value("${" + Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD + "}")
	private String employeeDefaultPassword;
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}
	
	@Bean
	public DataSource dataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
	}
	
	@Bean
	public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
	
	private String hashpw(String password) {
		String salt = BCrypt.gensalt(10, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}

	/**
	 * 预置数据，WebTestData中的User已经与Role建立了关联，所以设置了Role的id影响的是同一个
	 * @return
	 */
	@Bean
	public WebTestData webTestData() {
		WebTestData td = new WebTestData();
		
		td.role_admin.setId(id.getAndIncrement());
		roleDB.put(td.role_admin.getId(), td.role_admin);
		roleNameDB.put(td.role_admin.getName(), td.role_admin);
		
		td.role_manager.setId(id.getAndIncrement());
		roleDB.put(td.role_manager.getId(), td.role_manager);
		roleNameDB.put(td.role_manager.getName(), td.role_manager);
		
		td.role_staff.setId(id.getAndIncrement());
		roleDB.put(td.role_staff.getId(), td.role_staff);
		roleNameDB.put(td.role_staff.getName(), td.role_staff);
		
		td.role_guest.setId(id.getAndIncrement());
		roleDB.put(td.role_guest.getId(), td.role_guest);
		roleNameDB.put(td.role_guest.getName(), td.role_guest);
		
		td.user_admin.setId(id.getAndIncrement());
		td.user_admin.setPassword(hashpw(employeeDefaultPassword));
		userDB.put(td.user_admin.getId(), td.user_admin);
		
		td.user_bot.setId(id.getAndIncrement());
		td.user_bot.setPassword(hashpw(employeeDefaultPassword));
		userDB.put(td.user_bot.getId(), td.user_bot);
		
		td.user_anonymous.setId(id.getAndIncrement());
		td.user_anonymous.setPassword(hashpw(customerDefaultPassword));
		userDB.put(td.user_anonymous.getId(), td.user_anonymous);
		
		td.user_emailtohl.setId(id.getAndIncrement());
		td.user_emailtohl.setPassword(hashpw(customerDefaultPassword));
		userDB.put(td.user_emailtohl.getId(), td.user_emailtohl);
		
		td.foo.setId(id.getAndIncrement());
		td.foo.setPassword(hashpw(employeeDefaultPassword));
		userDB.put(td.foo.getId(), td.foo);
		
		td.bar.setId(id.getAndIncrement());
		td.bar.setPassword(hashpw(employeeDefaultPassword));
		userDB.put(td.bar.getId(), td.bar);
		
		td.baz.setId(id.getAndIncrement());
		td.baz.setPassword(hashpw(customerDefaultPassword));
		userDB.put(td.baz.getId(), td.baz);
		
		td.qux.setId(id.getAndIncrement());
		td.qux.setPassword(hashpw(customerDefaultPassword));
		userDB.put(td.qux.getId(), td.qux);
		
		return td;
	}
	
	@Bean
	public RoleService roleService(WebTestData td) {
		RoleService service = mock(RoleService.class);
		when(service.create(any())).thenAnswer(invocation -> {
			Role r = (Role) invocation.getArguments()[0];
			r.setId(id.getAndIncrement());
			roleDB.put(r.getId(), r);
			roleNameDB.put(r.getName(), r);
			return r;
		});
		when(service.get(any(Long.class))).thenAnswer(invocation -> {
			Long roleId = (Long) invocation.getArguments()[0];
			return roleDB.get(roleId);
		});
		when(service.get(any(String.class))).thenAnswer(invocation -> {
			String roleName = (String) invocation.getArguments()[0];
			return roleNameDB.get(roleName);
		});
		when(service.update(any(), any())).thenAnswer(invocation -> {
			Long roleId = (Long) invocation.getArguments()[0];
			Role target = roleDB.get(roleId);
			if (target != null) {
				Role source = (Role) invocation.getArguments()[1];
				BeanUtils.copyProperties(source, target, Role.getIgnoreProperties("name"));
			}
			return target;
		});
		doAnswer(invocation -> {
			Long roleId = (Long) invocation.getArguments()[0];
			Role r = roleDB.get(roleId);
			if (r != null && r.getName() != null) {
				roleNameDB.remove(r.getName());
			}
			roleDB.remove(roleId);
			return invocation.getMock();
		}).when(service).delete(any(Long.class));
		return service;
	}
	
	@Bean
	public CustomerRepository customerRepository(WebTestData td) {
		CustomerRepository dao = mock(CustomerRepository.class);
		when(dao.save(any(Customer.class))).then(invocation -> {
			Customer c = (Customer) invocation.getArguments()[0];
			c.setId(id.incrementAndGet());
			userDB.put(c.getId(), c);
			return c;
		});
		when(dao.get(any(Long.class))).then(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			return userDB.get(userId);
		});
		doAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			userDB.remove(userId);
			return invocation.getMock();
		}).when(dao).delete(any(Long.class));
		when(dao.findByUsername(anyString())).then(invocation -> {
			String username = (String) invocation.getArguments()[0];
			Matcher m = Constant.PATTERN_EMAIL.matcher(username);
			Customer result = null;
			if (m.matches()) {
				for (User u : userDB.values()) {
					if (u instanceof Customer && username.equals(u.getEmail())) {
						result = (Customer) u;
						break;
					}
				}
			} else {
				for (User u : userDB.values()) {
					if (u instanceof Customer && username.equals(u.getCellPhone())) {
						result = (Customer) u;
						break;
					}
				}
			}
			return result;
		});
		// 手机号码和邮箱都能查找到
		when(dao.findByCellPhone(td.user_emailtohl.getCellPhone())).thenReturn(td.user_emailtohl);
		when(dao.findByEmail(td.user_emailtohl.getEmail())).thenReturn(td.user_emailtohl);
		when(dao.findByCellPhone(td.baz.getCellPhone())).thenReturn(td.baz);
		when(dao.findByEmail(td.baz.getEmail())).thenReturn(td.baz);
		return dao;
	}
	
	@Bean
	public EmployeeRepository employeeRepository(WebTestData td) {
		EmployeeRepository dao = mock(EmployeeRepository.class);
		when(dao.save(any(Employee.class))).then(invocation -> {
			Employee e = (Employee) invocation.getArguments()[0];
			e.setId(id.incrementAndGet());
			userDB.put(e.getId(), e);
			return e;
		});
		when(dao.get(any(Long.class))).then(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			return userDB.get(userId);
		});
		doAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			userDB.remove(userId);
			return invocation.getMock();
		}).when(dao).delete(any(Long.class));
		when(dao.findByEmpNum(any())).then(invocation -> {
			Integer empNum = (Integer) invocation.getArguments()[0];
			User user = null;
			for (User u : userDB.values()) {
				if (u instanceof Employee && empNum.equals(((Employee) u).getEmpNum())) {
					user = u;
					break;
				}
			}
			return user;
		});
		return dao;
	}
	
	@Bean
	public CustomerService customerServiceMock(CustomerRepository re, WebTestData td) {
		CustomerService service = mock(CustomerService.class);
		when(service.create(any())).thenAnswer(invocation -> {
			Customer c = (Customer) invocation.getArguments()[0];
			return re.save(c);
		});
		when(service.get(any())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			return re.get(userId);
		});
		when(service.update(any(), any())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Customer target = re.get(userId);
			if (target != null) {
				Customer source = (Customer) invocation.getArguments()[1];
				BeanUtils.copyProperties(source, target, Customer.getIgnoreProperties("password", "roles", "enabled", "email", "cellPhone"));
			}
			return target;
		});
		doAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			re.delete(userId);
			return invocation.getMock();
		}).when(service).delete(any(Long.class));
		when(service.findByUsername(td.user_emailtohl.getCellPhone())).thenReturn(td.user_emailtohl);
		when(service.findByUsername(td.user_emailtohl.getEmail())).thenReturn(td.user_emailtohl);
		when(service.findByUsername(td.baz.getCellPhone())).thenReturn(td.baz);
		when(service.findByUsername(td.baz.getEmail())).thenReturn(td.baz);
		
		when(service.grandRoles(anyLong(), anyVararg())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Customer target = re.get(userId);
			if (target != null && invocation.getArguments().length > 1) {
				Set<Role> roles = Arrays.stream(invocation.getArguments())
				.filter(arg -> arg instanceof String)
				.filter(roleName -> roleNameDB.get(roleName) != null)
				.map(roleName -> roleNameDB.get(roleName))
				.collect(Collectors.toSet());
				target.getRoles().clear();
				target.getRoles().addAll(roles);
			}
			return target;
		});
		when(service.grandLevel(anyLong(), any(Customer.Level.class))).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Customer target = re.get(userId);
			if (target != null) {
				target.setLevel((Customer.Level) invocation.getArguments()[1]);
			}
			return target;
		});
		when(service.enabled(anyLong(), anyBoolean())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Customer target = re.get(userId);
			if (target != null) {
				target.setEnabled((Boolean) invocation.getArguments()[1]);
			}
			return target;
		});
		List<Customer> ls = userDB.values().stream().filter(u -> u instanceof Customer).map(u -> (Customer) u).collect(Collectors.toList());
		when(service.query(any())).thenReturn(ls);
		when(service.query(any(), any())).thenReturn(new Paging<>(ls));
		when(service.search(any(), any())).thenReturn(new Paging<>(ls));
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", td.baz));
		when(service.updatePassword(anyString(), anyString(), anyString())).thenReturn(new ExecResult(true, "", td.baz));
		when(service.exist(anyString())).thenReturn(true);
		when(service.getToken(anyString())).thenReturn("token_str");
		return service;
	}
	
	int empNum = Employee.NO1 + 100;
	@Bean
	public EmployeeService employeeServiceMock(EmployeeRepository re, WebTestData td) {
		EmployeeService service = mock(EmployeeService.class);
		when(service.create(any())).thenAnswer(invocation -> {
			Employee e = (Employee) invocation.getArguments()[0];
			e.setEmpNum(empNum++);
			return re.save(e);
		});
		when(service.get(any())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			return re.get(userId);
		});
		when(service.update(any(), any())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Employee target = re.get(userId);
			if (target != null) {
				Employee source = (Employee) invocation.getArguments()[1];
				BeanUtils.copyProperties(source, target, Employee.getIgnoreProperties("password", "empNum", "email", "cellPhone", "roles", "enabled"));
			}
			return target;
		});
		doAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			re.delete(userId);
			return invocation.getMock();
		}).when(service).delete(any(Long.class));
		when(service.grandRoles(anyLong(), anyVararg())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Employee target = re.get(userId);
			if (target != null && invocation.getArguments().length > 1) {
				Set<Role> roles = Arrays.stream(invocation.getArguments())
				.filter(arg -> arg instanceof String)
				.filter(roleName -> roleNameDB.get(roleName) != null)
				.map(roleName -> roleNameDB.get(roleName))
				.collect(Collectors.toSet());
				target.getRoles().clear();
				target.getRoles().addAll(roles);
			}
			return target;
		});
		when(service.enabled(anyLong(), anyBoolean())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Employee target = re.get(userId);
			if (target != null) {
				target.setEnabled((Boolean) invocation.getArguments()[1]);
			}
			return target;
		});
		List<Employee> ls = userDB.values().stream().filter(u -> u instanceof Employee).map(u -> (Employee) u).collect(Collectors.toList());
		when(service.query(any())).thenReturn(ls);
		when(service.query(any(), any())).thenReturn(new Paging<>(ls));
		when(service.search(any(), any())).thenReturn(new Paging<>(ls));
		when(service.getByEmpNum(any())).then(invocation -> {
			Integer empNum = (Integer) invocation.getArguments()[0];
			return re.findByEmpNum(empNum);
		});
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", td.bar));
		when(service.updatePassword(any(), anyString(), anyString())).thenReturn(new ExecResult(true, "", td.bar));
		when(service.enabled(anyLong(), anyBoolean())).thenReturn(td.bar);
		return service;
	}
	
	@Bean
	public Gson gson() {
		return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				return false;
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				if (clazz == byte[].class) {
					return true;
				}
				return false;
			}
		}).setDateFormat(Constant.TIME_STAMP).create();
	}
}
