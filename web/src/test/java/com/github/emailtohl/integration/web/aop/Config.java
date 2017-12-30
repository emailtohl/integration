package com.github.emailtohl.integration.web.aop;

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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
import com.github.emailtohl.integration.web.WebTestData;

/**
 * 尽量在真实环境中，包括事务层、缓存层、但没有包括安全层
 * 
 * @author HeLei
 *
 */
@Configurable
@ComponentScan("com.github.emailtohl.integration.web.aop")
@EnableTransactionManagement
@EnableCaching
@EnableAspectJAutoProxy
@PropertySource({ "classpath:config.properties" })
class Config {
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
			roleDB.remove(roleId);
			roleNameDB.remove(roleId);
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
			Employee c = (Employee) invocation.getArguments()[0];
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
		when(dao.findByEmpNum(Employee.NO1 + 1)).thenReturn(td.foo);
		when(dao.findByEmpNum(Employee.NO1 + 2)).thenReturn(td.bar);
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
		when(service.findByCellPhoneOrEmail(td.user_emailtohl.getCellPhone())).thenReturn(td.user_emailtohl);
		when(service.findByCellPhoneOrEmail(td.user_emailtohl.getEmail())).thenReturn(td.user_emailtohl);
		when(service.findByCellPhoneOrEmail(td.baz.getCellPhone())).thenReturn(td.baz);
		when(service.findByCellPhoneOrEmail(td.baz.getEmail())).thenReturn(td.baz);
		
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
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.updatePassword(anyString(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		return service;
	}
	
	@Bean
	public EmployeeService employeeServiceMock(EmployeeRepository re, WebTestData td) {
		EmployeeService service = mock(EmployeeService.class);
		when(service.create(any())).thenAnswer(invocation -> {
			Employee e = (Employee) invocation.getArguments()[0];
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
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.enabled(anyLong(), anyBoolean())).thenReturn(td.bar);
		when(service.updatePassword(any(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		return service;
	}
	
	/**
	 * 基于内存的配置
	 * @return
	 */
	@Bean
	public StandaloneInMemProcessEngineConfiguration standaloneInMemProcessEngineConfiguration() {
		// 基于内存数据库，且有Activiti自行管理事务
		StandaloneInMemProcessEngineConfiguration cfg = new StandaloneInMemProcessEngineConfiguration();
		return cfg;
	}
	
	/**
	 * 可以集成到Spring管理的事务中
	 * 
	 * @return
	 */
	@Bean
	public SpringProcessEngineConfiguration processEngineConfiguration() {
		DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
		PlatformTransactionManager platformTransactionManager = new DataSourceTransactionManager(dataSource);
		SpringProcessEngineConfiguration cfg = new SpringProcessEngineConfiguration();
		cfg.setDataSource(dataSource);
		cfg.setTransactionManager(platformTransactionManager);
		cfg.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		cfg.setActivityFontName("宋体");
		cfg.setLabelFontName("宋体");
		return cfg;
	}

	@Bean
	public ProcessEngine processEngine(SpringProcessEngineConfiguration config) throws Exception {
		ProcessEngineFactoryBean factory = new ProcessEngineFactoryBean();
		factory.setProcessEngineConfiguration(config);
		return factory.getObject();
	}

	/**
	 * 提供了管理和控制发布包和流程定义的操作
	 * @param engine
	 * @return
	 */
	@Bean
	public RepositoryService repositoryService(ProcessEngine engine) {
		return engine.getRepositoryService();
	}

	/**
	 * 负责启动一个流程定义的新实例
	 * @param engine
	 * @return
	 */
	@Bean
	public RuntimeService runtimeService(ProcessEngine engine) {
		return engine.getRuntimeService();
	}

	/**
	 * 任务是由系统中真实人员执行的，它是Activiti这类BPMN引擎的核心功能之一。 所有与任务有关的功能都包含在TaskService中
	 * @param engine
	 * @return
	 */
	@Bean
	public TaskService taskService(ProcessEngine engine) {
		return engine.getTaskService();
	}

	/**
	 * 提供了Activiti引擎手机的所有历史数据
	 * @param engine
	 * @return
	 */
	@Bean
	public HistoryService historyService(ProcessEngine engine) {
		return engine.getHistoryService();
	}
	
	/**
	 * 管理（创建，更新，删除，查询...）群组和用户
	 * Activiti执行时并没有对用户进行检查，引擎不会校验系统中是否存在这个用户
	 * @param engine
	 * @return
	 */
	@Bean
	public IdentityService identityService(ProcessEngine engine) {
		return engine.getIdentityService();
	}
	
	/**
	 * 可选服务
	 * 提供了启动表单和任务表单两个概念。 启动表单会在流程实例启动之前展示给用户， 任务表单会在用户完成任务时展示。
	 * @param engine
	 * @return
	 */
	@Bean
	public FormService formService(ProcessEngine engine) {
		return engine.getFormService();
	}

	/**
	 * 可以查询数据库的表和表的元数据
	 * @param engine
	 * @return
	 */
	@Bean
	public ManagementService managementService(ProcessEngine engine) {
		return engine.getManagementService();
	}

	
	@Bean
	public Group role_admin(WebTestData td, IdentityService identityService) {
		Group g = identityService.newGroup(td.role_admin.getId().toString());
		g.setName(td.role_admin.getName());
		g.setType("内置角色");
		identityService.saveGroup(g);
		return g;
	}
	
	@Bean
	public Group role_manager(WebTestData td, IdentityService identityService) {
		Group g = identityService.newGroup(td.role_manager.getId().toString());
		g.setName(td.role_manager.getName());
		g.setType("内置角色");
		identityService.saveGroup(g);
		return g;
	}
	
	@Bean
	public Group role_staff(WebTestData td, IdentityService identityService) {
		Group g = identityService.newGroup(td.role_staff.getId().toString());
		g.setName(td.role_staff.getName());
		g.setType("内置角色");
		identityService.saveGroup(g);
		return g;
	}
	
	@Bean
	public Group role_guest(WebTestData td, IdentityService identityService) {
		Group g = identityService.newGroup(td.role_guest.getId().toString());
		g.setName(td.role_guest.getName());
		g.setType("内置角色");
		identityService.saveGroup(g);
		return g;
	}
	
	@Bean
	public User user_admin(WebTestData td, IdentityService identityService) {
		User u = identityService.newUser(td.user_admin.getId().toString());
		u.setId(td.user_admin.getId().toString());
		u.setFirstName(td.user_admin.getName());
		u.setLastName(td.user_admin.getNickname());
		u.setEmail(td.user_admin.getEmail());
		identityService.saveUser(u);
		return u;
	}
	
	@Bean
	public User user_bot(WebTestData td, IdentityService identityService) {
		User u = identityService.newUser(td.user_bot.getId().toString());
		u.setId(td.user_bot.getId().toString());
		u.setFirstName(td.user_bot.getName());
		u.setLastName(td.user_bot.getNickname());
		u.setEmail(td.user_bot.getEmail());
		identityService.saveUser(u);
		return u;
	}
	
	@Bean
	public User user_anonymous(WebTestData td, IdentityService identityService) {
		User u = identityService.newUser(td.user_anonymous.getId().toString());
		u.setId(td.user_anonymous.getId().toString());
		u.setFirstName(td.user_anonymous.getName());
		u.setLastName(td.user_anonymous.getNickname());
		u.setEmail(td.user_anonymous.getEmail());
		identityService.saveUser(u);
		return u;
	}
	
	@Bean
	public User user_emailtohl(WebTestData td, IdentityService identityService) {
		User u = identityService.newUser(td.user_emailtohl.getId().toString());
		u.setId(td.user_emailtohl.getId().toString());
		u.setFirstName(td.user_emailtohl.getName());
		u.setLastName(td.user_emailtohl.getNickname());
		u.setEmail(td.user_emailtohl.getEmail());
		identityService.saveUser(u);
		return u;
	}
	
}
