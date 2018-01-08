package com.github.emailtohl.integration.core.auth;

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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.github.emailtohl.integration.common.jpa.Paging;
import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.config.Constant;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleAuditedService;
import com.github.emailtohl.integration.core.role.RoleService;
import com.github.emailtohl.integration.core.user.UserRefRepository;
import com.github.emailtohl.integration.core.user.UserRepository;
import com.github.emailtohl.integration.core.user.UserService;
import com.github.emailtohl.integration.core.user.UserServiceImpl;
import com.github.emailtohl.integration.core.user.customer.CustomerAuditedService;
import com.github.emailtohl.integration.core.user.customer.CustomerRefRepository;
import com.github.emailtohl.integration.core.user.customer.CustomerRepository;
import com.github.emailtohl.integration.core.user.customer.CustomerService;
import com.github.emailtohl.integration.core.user.employee.EmployeeAuditedService;
import com.github.emailtohl.integration.core.user.employee.EmployeeRefRepository;
import com.github.emailtohl.integration.core.user.employee.EmployeeRepository;
import com.github.emailtohl.integration.core.user.employee.EmployeeService;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.User;

/**
 * 对接口安全权限的测试
 * @author HeLei
 */
@Configuration
@EnableCaching
// 为了在应用层而非过滤器中使用spring security，还可以启动@EnableGlobalMethodSecurity功能
// 这时候spring会在调用Bean方法时再添加一个切面，执行spring security的安全检查
@EnableGlobalMethodSecurity(prePostEnabled = true, order = 0, mode = AdviceMode.PROXY, proxyTargetClass = false)
@PropertySource({ "classpath:config.properties" })
class SecurityConfiguration {
	private AtomicLong id = new AtomicLong(1L);
	private Map<Long, Role> roleDB = new ConcurrentHashMap<>();
	private Map<String, Role> roleNameDB = new ConcurrentHashMap<>();
	private Map<Long, com.github.emailtohl.integration.core.user.entities.User> userDB = new ConcurrentHashMap<>();
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	private String hashpw(String password) {
		String salt = BCrypt.gensalt(10, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}
	
	@Value("${" + Constant.PROP_CUSTOMER_DEFAULT_PASSWORD + "}")
	private String customerDefaultPassword;
	@Value("${" + Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD + "}")
	private String employeeDefaultPassword;
	
	/**
	 * 预置数据，WebTestData中的User已经与Role建立了关联，所以设置了Role的id影响的是同一个
	 * @return
	 */
	@Bean
	public CoreTestData coreTestData() {
		CoreTestData td = new CoreTestData();
		
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
	
	/**
	 * 简单的缓存管理器的实现
	 * 
	 * @return
	 */
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}
	
	@Bean
	public CustomerRepository customerRepository(CoreTestData td) {
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
	public EmployeeRepository employeeRepository(CoreTestData td) {
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
	public CustomerRefRepository customerRefRepository() {
		CustomerRefRepository dao = mock(CustomerRefRepository.class);
		return dao;
	}
	
	@Bean
	public EmployeeRefRepository employeeRefRepository() {
		EmployeeRefRepository dao = mock(EmployeeRefRepository.class);
		return dao;
	}
	
	@Bean
	public UserRepository userRepository() {
		UserRepository dao = mock(UserRepository.class);
		return dao;
	}
	
	@Bean
	public UserRefRepository userRefRepository() {
		UserRefRepository dao = mock(UserRefRepository.class);
		return dao;
	}
	
	@Bean
	public UserService userService(CustomerRepository cr, EmployeeRepository er, CustomerRefRepository crr,
			EmployeeRefRepository err, UserRepository ur, UserRefRepository urr) {
		UserService service = new UserServiceImpl(cr, er, crr, err, ur, urr);
		return service;
	}

	@Bean
	public AuthenticationProvider authenticationProvider(UserService userService) {
		AuthenticationProviderImpl authenticationProviderImpl = new AuthenticationProviderImpl(userService);
		return authenticationProviderImpl;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(UserService userService) {
		AuthenticationManagerImpl authenticationManager = new AuthenticationManagerImpl(userService);
		return authenticationManager;
	}
	
	// AuthenticationManager来源于com.github.emailtohl.integration.user.auth
	@Bean
	public SecurityContextManager securityContextManager(AuthenticationManager authenticationManager, CoreTestData td) {
		return new SecurityContextManager(authenticationManager, td);
	}
	
	@Bean
	public RoleService roleServiceMock(CoreTestData td) {
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
	public RoleAuditedService roleAuditedServiceMock(CoreTestData td) {
		RoleAuditedService service = mock(RoleAuditedService.class);
		when(service.getRoleAtRevision(anyLong(), any())).thenReturn(td.role_guest);
		when(service.getRoleRevision(anyLong())).thenReturn(Arrays.asList(new Tuple<Role>()));
		return service;
	}
	
	@Bean
	public CustomerService customerServiceMock(CustomerRepository re, CoreTestData td) {
		CustomerService service = mock(CustomerService.class);
		when(service.create(any())).thenAnswer(invocation -> {
			Customer c = (Customer) invocation.getArguments()[0];
			return re.save(c);
		});
		when(service.get(any())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			return re.get(userId);
		});
		when(service.getUsernames(anyLong())).thenAnswer(invocation -> {
			Long userId = (Long) invocation.getArguments()[0];
			User u = re.get(userId);
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
		when(service.getByUsername(td.user_emailtohl.getCellPhone())).thenReturn(td.user_emailtohl);
		when(service.getByUsername(td.user_emailtohl.getEmail())).thenReturn(td.user_emailtohl);
		when(service.getByUsername(td.baz.getCellPhone())).thenReturn(td.baz);
		when(service.getByUsername(td.baz.getEmail())).thenReturn(td.baz);
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
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.updatePassword(anyString(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		return service;
	}
	
	int empNum = Employee.NO1 + 100;
	@Bean
	public EmployeeService employeeServiceMock(EmployeeRepository re, CoreTestData td) {
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
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.enabled(anyLong(), anyBoolean())).thenReturn(td.bar);
		when(service.updatePassword(any(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		return service;
	}
	
	@Bean
	public CustomerAuditedService customerAuditedServiceMock(CoreTestData td) {
		CustomerAuditedService service = mock(CustomerAuditedService.class);
		when(service.getCustomerAtRevision(anyLong(), any())).thenReturn(td.baz);
		when(service.getCustomerRevision(anyLong())).thenReturn(Arrays.asList(new Tuple<Customer>()));
		return service;
	}
	
	@Bean
	public EmployeeAuditedService employeeAuditedServiceMock(CoreTestData td) {
		EmployeeAuditedService service = mock(EmployeeAuditedService.class);
		when(service.getEmployeeAtRevision(anyLong(), any())).thenReturn(td.bar);
		when(service.getEmployeeRevision(anyLong())).thenReturn(Arrays.asList(new Tuple<Employee>()));
		return service;
	}
}

