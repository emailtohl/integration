package com.github.emailtohl.integration.core.auth;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import com.github.emailtohl.integration.common.jpa.envers.Tuple;
import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.core.auth.AuthenticationManagerImpl;
import com.github.emailtohl.integration.core.auth.AuthenticationProviderImpl;
import com.github.emailtohl.integration.core.coreTestConfig.CoreTestData;
import com.github.emailtohl.integration.core.user.dao.CustomerRepository;
import com.github.emailtohl.integration.core.user.dao.EmployeeRepository;
import com.github.emailtohl.integration.core.user.dao.UserRepository;
import com.github.emailtohl.integration.core.user.entities.Customer;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.core.user.entities.Role;
import com.github.emailtohl.integration.core.user.service.CustomerAuditedService;
import com.github.emailtohl.integration.core.user.service.CustomerService;
import com.github.emailtohl.integration.core.user.service.EmployeeAuditedService;
import com.github.emailtohl.integration.core.user.service.EmployeeService;
import com.github.emailtohl.integration.core.user.service.RoleAuditedService;
import com.github.emailtohl.integration.core.user.service.RoleService;
import com.github.emailtohl.integration.core.user.service.UserService;
import com.github.emailtohl.integration.core.user.service.UserServiceImpl;

/**
 * 对接口安全权限的测试
 * @author HeLei
 */
@Configuration
@EnableCaching
// 为了在应用层而非过滤器中使用spring security，还可以启动@EnableGlobalMethodSecurity功能
// 这时候spring会在调用Bean方法时再添加一个切面，执行spring security的安全检查
@EnableGlobalMethodSecurity(prePostEnabled = true, order = 0, mode = AdviceMode.PROXY, proxyTargetClass = false)
class SecurityConfiguration {
	private CoreTestData td = new CoreTestData();
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
	public CustomerRepository customerRepository() {
		CustomerRepository dao = mock(CustomerRepository.class);
		// 手机号码和邮箱都能查找到
		when(dao.findByCellPhone(td.emailtohl.getCellPhone())).thenReturn(td.emailtohl);
		when(dao.findByEmail(td.emailtohl.getEmail())).thenReturn(td.emailtohl);
		when(dao.findByCellPhone(td.baz.getCellPhone())).thenReturn(td.baz);
		when(dao.findByEmail(td.baz.getEmail())).thenReturn(td.baz);
		return dao;
	}
	
	@Bean
	public EmployeeRepository employeeRepository() {
		EmployeeRepository dao = mock(EmployeeRepository.class);
		when(dao.findByEmpNum(Employee.NO1 + 1)).thenReturn(td.foo);
		when(dao.findByEmpNum(Employee.NO1 + 2)).thenReturn(td.bar);
		return dao;
	}
	
	@Bean
	public UserRepository userRepository() {
		UserRepository dao = mock(UserRepository.class);
		return dao;
	}
	
	@Bean
	public UserService userService(CustomerRepository cr, EmployeeRepository er, UserRepository ur) {
		UserService service = new UserServiceImpl(cr, er, ur);
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
	public RoleAuditedService roleAuditedServiceMock() {
		RoleAuditedService service = mock(RoleAuditedService.class);
		when(service.getRoleAtRevision(anyLong(), any())).thenReturn(td.role_guest);
		when(service.getRoleRevision(anyLong())).thenReturn(Arrays.asList(new Tuple<Role>()));
		return service;
	}
	
	@Bean
	public CustomerService customerServiceMock() {
		CustomerService service = mock(CustomerService.class);
		when(service.grandRoles(anyLong(), anyVararg())).thenReturn(td.baz);
		when(service.grandLevel(anyLong(), any(Customer.Level.class))).thenReturn(td.baz);
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.enabled(anyLong(), anyBoolean())).thenReturn(td.baz);
		when(service.updatePassword(anyString(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		return service;
	}
	
	@Bean
	public EmployeeService employeeServiceMock() {
		EmployeeService service = mock(EmployeeService.class);
		when(service.grandRoles(anyLong(), anyVararg())).thenReturn(td.bar);
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.enabled(anyLong(), anyBoolean())).thenReturn(td.bar);
		when(service.updatePassword(any(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		return service;
	}
	
	@Bean
	public CustomerAuditedService customerAuditedServiceMock() {
		CustomerAuditedService service = mock(CustomerAuditedService.class);
		when(service.getCustomerAtRevision(anyLong(), any())).thenReturn(td.baz);
		when(service.getCustomerRevision(anyLong())).thenReturn(Arrays.asList(new Tuple<Customer>()));
		return service;
	}
	
	@Bean
	public EmployeeAuditedService employeeAuditedServiceMock() {
		EmployeeAuditedService service = mock(EmployeeAuditedService.class);
		when(service.getEmployeeAtRevision(anyLong(), any())).thenReturn(td.bar);
		when(service.getEmployeeRevision(anyLong())).thenReturn(Arrays.asList(new Tuple<Employee>()));
		return service;
	}
}

