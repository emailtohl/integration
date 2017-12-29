package com.github.emailtohl.integration.core.auth;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.util.Arrays;

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
	public static final Long EMAIL_TO_HL_ID = 8L, FOO_ID = 9L, BAR_ID = 10L, BAZ_ID = 11L, QUX_ID = 12L;
	
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
	 * 预置数据
	 * @return
	 */
	@Bean
	public CoreTestData coreTestData() {
		CoreTestData td = new CoreTestData();
		td.role_admin.setId(1L);
		td.role_manager.setId(2L);
		td.role_staff.setId(3L);
		td.role_guest.setId(4L);
		td.user_admin.setId(5L);
		td.user_emailtohl.setPassword(hashpw(employeeDefaultPassword));
		td.user_bot.setId(6L);
		td.user_bot.setPassword(hashpw(employeeDefaultPassword));
		td.user_anonymous.setId(7L);
		td.user_anonymous.setPassword(hashpw(customerDefaultPassword));
		td.user_emailtohl.setId(EMAIL_TO_HL_ID);
		td.user_emailtohl.setPassword(hashpw(customerDefaultPassword));
		td.foo.setId(FOO_ID);
		td.foo.setPassword(hashpw(employeeDefaultPassword));
		td.bar.setId(BAR_ID);
		td.bar.setPassword(hashpw(employeeDefaultPassword));
		td.baz.setId(BAZ_ID);
		td.baz.setPassword(hashpw(customerDefaultPassword));
		td.qux.setId(QUX_ID);
		td.qux.setPassword(hashpw(customerDefaultPassword));
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
		// 手机号码和邮箱都能查找到
		when(dao.findByCellPhone(td.user_emailtohl.getCellPhone())).thenReturn(td.user_emailtohl);
		when(dao.findByEmail(td.user_emailtohl.getEmail())).thenReturn(td.user_emailtohl);
		when(dao.findByCellPhone(td.baz.getCellPhone())).thenReturn(td.baz);
		when(dao.findByEmail(td.baz.getEmail())).thenReturn(td.baz);
		when(dao.get(EMAIL_TO_HL_ID)).thenReturn(td.user_emailtohl);
		when(dao.get(BAZ_ID)).thenReturn(td.baz);
		when(dao.get(QUX_ID)).thenReturn(td.qux);
		return dao;
	}
	
	@Bean
	public EmployeeRepository employeeRepository(CoreTestData td) {
		EmployeeRepository dao = mock(EmployeeRepository.class);
		when(dao.findByEmpNum(Employee.NO1 + 1)).thenReturn(td.foo);
		when(dao.findByEmpNum(Employee.NO1 + 2)).thenReturn(td.bar);
		when(dao.get(FOO_ID)).thenReturn(td.foo);
		when(dao.get(BAR_ID)).thenReturn(td.bar);
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
	public SecurityContextManager securityContextManager(AuthenticationManager authenticationManager) {
		return new SecurityContextManager(authenticationManager);
	}
	
	@Bean
	public RoleService roleServiceMock() {
		RoleService service = mock(RoleService.class);
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
	public CustomerService customerServiceMock(CoreTestData td) {
		CustomerService service = mock(CustomerService.class);
		when(service.create(any())).thenAnswer(invocation -> {
			Customer c = (Customer) invocation.getArguments()[0];
			c.setId(10001L);
			return c;
		});
		when(service.update(any(), any())).thenAnswer(invocation -> {
			Customer c = (Customer) invocation.getArguments()[1];
			c.setId((Long) invocation.getArguments()[0]);
			return c;
		});
		when(service.get(EMAIL_TO_HL_ID)).thenReturn(td.user_emailtohl);
		when(service.get(BAZ_ID)).thenReturn(td.baz);
		when(service.get(QUX_ID)).thenReturn(td.qux);
		when(service.findByCellPhoneOrEmail(td.user_emailtohl.getCellPhone())).thenReturn(td.user_emailtohl);
		when(service.findByCellPhoneOrEmail(td.user_emailtohl.getEmail())).thenReturn(td.user_emailtohl);
		when(service.findByCellPhoneOrEmail(td.baz.getCellPhone())).thenReturn(td.baz);
		when(service.findByCellPhoneOrEmail(td.baz.getEmail())).thenReturn(td.baz);
		when(service.grandRoles(anyLong(), anyVararg())).thenReturn(td.baz);
		when(service.grandLevel(anyLong(), any(Customer.Level.class))).thenReturn(td.baz);
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.enabled(anyLong(), anyBoolean())).thenReturn(td.baz);
		when(service.updatePassword(anyString(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		return service;
	}
	
	@Bean
	public EmployeeService employeeServiceMock(CoreTestData td) {
		EmployeeService service = mock(EmployeeService.class);
		when(service.create(any())).thenAnswer(invocation -> {
			Employee e = (Employee) invocation.getArguments()[0];
			e.setId(10000L);
			return e;
		});
		when(service.update(any(), any())).thenAnswer(invocation -> {
			Employee e = (Employee) invocation.getArguments()[1];
			e.setId((Long) invocation.getArguments()[0]);
			return e;
		});
		when(service.get(FOO_ID)).thenReturn(td.foo);
		when(service.get(BAR_ID)).thenReturn(td.bar);
		when(service.grandRoles(anyLong(), anyVararg())).thenReturn(td.bar);
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

