package com.github.emailtohl.integration.web.aop;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;

import javax.annotation.PostConstruct;
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
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
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
	public static final Long EMAIL_TO_HL_ID = 1L, FOO_ID = 2L, BAR_ID = 3L, BAZ_ID = 4L, QUX_ID = 5L;
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	private String hashpw(String password) {
		String salt = BCrypt.gensalt(10, new SecureRandom());
		return BCrypt.hashpw(password, salt);
	}
	
	private WebTestData td = new WebTestData();
	
	@Value("${" + Constant.PROP_CUSTOMER_DEFAULT_PASSWORD + "}")
	private String customerDefaultPassword;
	@Value("${" + Constant.PROP_EMPLOYEE_DEFAULT_PASSWORD + "}")
	private String employeeDefaultPassword;
	
	@PostConstruct
	public void init() {
		td.user_emailtohl.setId(1L);
		td.user_emailtohl.setPassword(hashpw(customerDefaultPassword));
		td.foo.setId(2L);
		td.foo.setPassword(hashpw(employeeDefaultPassword));
		td.bar.setId(3L);
		td.bar.setPassword(hashpw(employeeDefaultPassword));
		td.baz.setId(4L);
		td.baz.setPassword(hashpw(customerDefaultPassword));
		td.qux.setId(5L);
		td.qux.setPassword(hashpw(customerDefaultPassword));
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
	public CustomerRepository customerRepository() {
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
	public EmployeeRepository employeeRepository() {
		EmployeeRepository dao = mock(EmployeeRepository.class);
		when(dao.findByEmpNum(Employee.NO1 + 1)).thenReturn(td.foo);
		when(dao.findByEmpNum(Employee.NO1 + 2)).thenReturn(td.bar);
		when(dao.get(FOO_ID)).thenReturn(td.foo);
		when(dao.get(BAR_ID)).thenReturn(td.bar);
		return dao;
	}
	
	@Bean
	public CustomerService customerServiceMock() {
		CustomerService service = mock(CustomerService.class);
		when(service.create(any())).thenReturn(td.baz);
		when(service.update(any(), any())).thenReturn(td.baz);
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
	public EmployeeService employeeServiceMock() {
		EmployeeService service = mock(EmployeeService.class);
		when(service.create(any())).thenReturn(td.foo);
		when(service.update(any(), any())).thenReturn(td.foo);
		when(service.get(FOO_ID)).thenReturn(td.foo);
		when(service.get(BAR_ID)).thenReturn(td.bar);
		when(service.grandRoles(anyLong(), anyVararg())).thenReturn(td.bar);
		when(service.resetPassword(anyLong())).thenReturn(new ExecResult(true, "", null));
		when(service.enabled(anyLong(), anyBoolean())).thenReturn(td.bar);
		when(service.updatePassword(any(), anyString(), anyString())).thenReturn(new ExecResult(true, "", null));
		return service;
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
		SpringProcessEngineConfiguration c = new SpringProcessEngineConfiguration();
		c.setDataSource(dataSource);
		c.setTransactionManager(platformTransactionManager);
		c.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		c.setActivityFontName("宋体");
		c.setLabelFontName("宋体");
		return c;
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

	
}
