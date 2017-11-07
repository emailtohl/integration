package com.github.emailtohl.integration.nuser.auth;

import static org.mockito.Mockito.*;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import com.github.emailtohl.integration.common.standard.ExecResult;
import com.github.emailtohl.integration.nuser.UserTestData;
import com.github.emailtohl.integration.nuser.dao.CustomerRepository;
import com.github.emailtohl.integration.nuser.dao.EmployeeRepository;
import com.github.emailtohl.integration.nuser.entities.Customer;
import com.github.emailtohl.integration.nuser.entities.Employee;
import com.github.emailtohl.integration.nuser.service.CustomerAuditedService;
import com.github.emailtohl.integration.nuser.service.CustomerService;
import com.github.emailtohl.integration.nuser.service.EmployeeAuditedService;
import com.github.emailtohl.integration.nuser.service.EmployeeService;
import com.github.emailtohl.integration.nuser.service.RoleAuditedService;
import com.github.emailtohl.integration.nuser.service.RoleService;

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
	private UserTestData td = new UserTestData();
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
	public LoadUser loadUser(CustomerRepository cr, EmployeeRepository er) {
		LoadUser l = new LoadUser(cr, er);
		return l;
	}

	@Bean
	public AuthenticationProvider authenticationProvider(LoadUser l) {
		AuthenticationProviderImpl authenticationProviderImpl = new AuthenticationProviderImpl(l);
		return authenticationProviderImpl;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(LoadUser l) {
		AuthenticationManagerImpl authenticationManager = new AuthenticationManagerImpl(l);
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
		return service;
	}
	
	@Bean
	public CustomerService customerServiceMock() {
		CustomerService service = mock(CustomerService.class);
		when(service.grandRoles(anyLong(), anyVararg())).thenReturn(td.baz);
		when(service.grandLevel(anyLong(), any(Customer.Level.class))).thenReturn(td.baz);
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.lock(anyLong(), anyBoolean())).thenReturn(td.baz);
		return service;
	}
	
	@Bean
	public EmployeeService employeeServiceMock() {
		EmployeeService service = mock(EmployeeService.class);
		when(service.grandRoles(anyLong(), anyVararg())).thenReturn(td.bar);
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.lock(anyLong(), anyBoolean())).thenReturn(td.bar);
		return service;
	}
	
	@Bean
	public CustomerAuditedService customerAuditedServiceMock() {
		CustomerAuditedService service = mock(CustomerAuditedService.class);
		return service;
	}
	
	@Bean
	public EmployeeAuditedService employeeAuditedServiceMock() {
		EmployeeAuditedService service = mock(EmployeeAuditedService.class);
		return service;
	}
}

