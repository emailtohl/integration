package com.github.emailtohl.integration.web.config;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.util.ArrayList;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.user.customer.CustomerRepository;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeRepository;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;
import com.github.emailtohl.lib.jpa.Paging;
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
public class MockConfig {
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
	 * 预置数据，PresetData中的User已经与Role建立了关联，所以设置了Role的id影响的是同一个
	 * @return
	 */
	@Bean
	public CorePresetData presetData() {
		CorePresetData pd = new CorePresetData();
		
		pd.role_admin.setId(id.getAndIncrement());
		roleDB.put(pd.role_admin.getId(), pd.role_admin);
		roleNameDB.put(pd.role_admin.getName(), pd.role_admin);
		
		pd.role_manager.setId(id.getAndIncrement());
		roleDB.put(pd.role_manager.getId(), pd.role_manager);
		roleNameDB.put(pd.role_manager.getName(), pd.role_manager);
		
		pd.role_staff.setId(id.getAndIncrement());
		roleDB.put(pd.role_staff.getId(), pd.role_staff);
		roleNameDB.put(pd.role_staff.getName(), pd.role_staff);
		
		pd.role_guest.setId(id.getAndIncrement());
		roleDB.put(pd.role_guest.getId(), pd.role_guest);
		roleNameDB.put(pd.role_guest.getName(), pd.role_guest);
		
		pd.user_admin.setId(id.getAndIncrement());
		pd.user_admin.setPassword(hashpw(employeeDefaultPassword));
		userDB.put(pd.user_admin.getId(), pd.user_admin);
		
		pd.user_bot.setId(id.getAndIncrement());
		pd.user_bot.setPassword(hashpw(employeeDefaultPassword));
		userDB.put(pd.user_bot.getId(), pd.user_bot);
		
		pd.user_anonymous.setId(id.getAndIncrement());
		pd.user_anonymous.setPassword(hashpw(customerDefaultPassword));
		userDB.put(pd.user_anonymous.getId(), pd.user_anonymous);
		
		pd.user_emailtohl.setId(id.getAndIncrement());
		pd.user_emailtohl.setPassword(hashpw(customerDefaultPassword));
		userDB.put(pd.user_emailtohl.getId(), pd.user_emailtohl);
		return pd;
	}
	
	@Bean
	public WebTestData webTestData(CorePresetData pd) {
		WebTestData td = new WebTestData(pd);

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
	public RoleService roleService() {
		RoleService service = mock(RoleService.class);
		when(service.create(any())).thenAnswer(invocation -> {
			Role r = (Role) invocation.getArguments()[0];
			r.setId(id.getAndIncrement());
			roleDB.put(r.getId(), r);
			roleNameDB.put(r.getName(), r);
			return r;
		});
		when(service.get(anyLong())).thenAnswer(invocation -> {
			Long roleId = (Long) invocation.getArguments()[0];
			return roleDB.get(roleId);
		});
		when(service.get(anyString())).thenAnswer(invocation -> {
			String roleName = (String) invocation.getArguments()[0];
			return roleNameDB.get(roleName);
		});
		when(service.getRoleName(anyLong())).thenAnswer(invocation -> {
			Long roleId = (Long) invocation.getArguments()[0];
			Role r = roleDB.get(roleId);
			if (r == null) {
				return null;
			}
			return r.getName();
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
		when(dao.find(any(Long.class))).then(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			return userDB.get(userId);
		});
		doAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			userDB.remove(userId);
			return invocation.getMock();
		}).when(dao).deleteById(any(Long.class));
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
		when(dao.findByCellPhone(td.pd.user_emailtohl.getCellPhone())).thenReturn(td.pd.user_emailtohl);
		when(dao.findByEmail(td.pd.user_emailtohl.getEmail())).thenReturn(td.pd.user_emailtohl);
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
		when(dao.find(any(Long.class))).then(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			return userDB.get(userId);
		});
		doAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			userDB.remove(userId);
			return invocation.getMock();
		}).when(dao).deleteById(any(Long.class));
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
			return re.find(userId);
		});
		when(service.getUsernames(anyLong())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			User u = re.find(userId);
			List<String> usernames;
			if (u instanceof Customer) {
				usernames = new ArrayList<String>(((Customer) u).getUsernames());
			} else {
				usernames = new ArrayList<String>();
			}
			return usernames;
		});
		when(service.update(any(), any())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Customer target = re.find(userId);
			if (target != null) {
				Customer source = (Customer) invocation.getArguments()[1];
				BeanUtils.copyProperties(source, target, Customer.getIgnoreProperties("password", "roles", "enabled", "email", "cellPhone"));
			}
			return target;
		});
		doAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			re.deleteById(userId);
			return invocation.getMock();
		}).when(service).delete(any(Long.class));
		when(service.getByUsername(td.pd.user_emailtohl.getCellPhone())).thenReturn(td.pd.user_emailtohl);
		when(service.getByUsername(td.pd.user_emailtohl.getEmail())).thenReturn(td.pd.user_emailtohl);
		when(service.getByUsername(td.baz.getCellPhone())).thenReturn(td.baz);
		when(service.getByUsername(td.baz.getEmail())).thenReturn(td.baz);
		
		when(service.grandRoles(anyLong(), anyVararg())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Customer target = re.find(userId);
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
			Customer target = re.find(userId);
			if (target != null) {
				target.setLevel((Customer.Level) invocation.getArguments()[1]);
			}
			return target;
		});
		when(service.enabled(anyLong(), anyBoolean())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Customer target = re.find(userId);
			if (target != null) {
				target.setEnabled((Boolean) invocation.getArguments()[1]);
			}
			return target;
		});
		List<Customer> ls = userDB.values().stream().filter(u -> u instanceof Customer).map(u -> (Customer) u).collect(Collectors.toList());
		Pageable pageable = PageRequest.of(0, 20);
		when(service.query(any())).thenReturn(ls);
		when(service.query(any(), any())).thenReturn(new Paging<>(ls, pageable, ls.size()));
		when(service.search(any(), any())).thenReturn(new Paging<>(ls, pageable, ls.size()));
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
			return re.find(userId);
		});
		when(service.update(any(), any())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Employee target = re.find(userId);
			if (target != null) {
				Employee source = (Employee) invocation.getArguments()[1];
				BeanUtils.copyProperties(source, target, Employee.getIgnoreProperties("password", "empNum", "email", "cellPhone", "roles", "enabled"));
			}
			return target;
		});
		doAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			re.deleteById(userId);
			return invocation.getMock();
		}).when(service).delete(any(Long.class));
		when(service.grandRoles(anyLong(), anyVararg())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			Employee target = re.find(userId);
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
			Employee target = re.find(userId);
			if (target != null) {
				target.setEnabled((Boolean) invocation.getArguments()[1]);
			}
			return target;
		});
		List<Employee> ls = userDB.values().stream().filter(u -> u instanceof Employee).map(u -> (Employee) u).collect(Collectors.toList());
		Pageable pageable = PageRequest.of(0, 20);
		when(service.query(any())).thenReturn(ls);
		when(service.query(any(), any())).thenReturn(new Paging<>(ls, pageable, ls.size()));
		when(service.search(any(), any())).thenReturn(new Paging<>(ls, pageable, ls.size()));
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
