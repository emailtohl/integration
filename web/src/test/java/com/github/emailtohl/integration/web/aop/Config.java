package com.github.emailtohl.integration.web.aop;

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
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.web.MockConfig;

/**
 * 尽量在真实环境中，包括事务层、缓存层、但没有包括安全层
 * 
 * 现在此配置未在使用，aop的测试统一在同一个环境中测试
 * 
 * @author HeLei
 *
 */
@Configurable
@Import(MockConfig.class)
@ComponentScan("com.github.emailtohl.integration.web.aop")
@EnableAspectJAutoProxy
class Config {
	
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
	public SpringProcessEngineConfiguration processEngineConfiguration(DataSource dataSource, PlatformTransactionManager platformTransactionManager) {
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

	// -----------下面是初始化内置数据-------------------
	@Bean
	public Group role_admin(CorePresetData pd, IdentityService identityService) {
		Group g = identityService.newGroup(pd.role_admin.getId().toString());
		g.setName(pd.role_admin.getName());
		g.setType("内置角色");
		identityService.saveGroup(g);
		return g;
	}
	
	@Bean
	public Group role_manager(CorePresetData pd, IdentityService identityService) {
		Group g = identityService.newGroup(pd.role_manager.getId().toString());
		g.setName(pd.role_manager.getName());
		g.setType("内置角色");
		identityService.saveGroup(g);
		return g;
	}
	
	@Bean
	public Group role_staff(CorePresetData pd, IdentityService identityService) {
		Group g = identityService.newGroup(pd.role_staff.getId().toString());
		g.setName(pd.role_staff.getName());
		g.setType("内置角色");
		identityService.saveGroup(g);
		return g;
	}
	
	@Bean
	public Group role_guest(CorePresetData pd, IdentityService identityService) {
		Group g = identityService.newGroup(pd.role_guest.getId().toString());
		g.setName(pd.role_guest.getName());
		g.setType("内置角色");
		identityService.saveGroup(g);
		return g;
	}
	
	@Bean
	public User user_admin(CorePresetData pd, IdentityService identityService) {
		User u = identityService.newUser(pd.user_admin.getId().toString());
		u.setId(pd.user_admin.getId().toString());
		u.setFirstName(pd.user_admin.getName());
		u.setLastName(pd.user_admin.getNickname());
		u.setEmail(pd.user_admin.getEmail());
		identityService.saveUser(u);
		return u;
	}
	
	@Bean
	public User user_bot(CorePresetData pd, IdentityService identityService) {
		User u = identityService.newUser(pd.user_bot.getId().toString());
		u.setId(pd.user_bot.getId().toString());
		u.setFirstName(pd.user_bot.getName());
		u.setLastName(pd.user_bot.getNickname());
		u.setEmail(pd.user_bot.getEmail());
		identityService.saveUser(u);
		return u;
	}
	
	@Bean
	public User user_anonymous(CorePresetData pd, IdentityService identityService) {
		User u = identityService.newUser(pd.user_anonymous.getId().toString());
		u.setId(pd.user_anonymous.getId().toString());
		u.setFirstName(pd.user_anonymous.getName());
		u.setLastName(pd.user_anonymous.getNickname());
		u.setEmail(pd.user_anonymous.getEmail());
		identityService.saveUser(u);
		return u;
	}
	
	@Bean
	public User user_emailtohl(CorePresetData pd, IdentityService identityService) {
		User u = identityService.newUser(pd.user_emailtohl.getId().toString());
		u.setId(pd.user_emailtohl.getId().toString());
		u.setFirstName(pd.user_emailtohl.getName());
		u.setLastName(pd.user_emailtohl.getNickname());
		u.setEmail(pd.user_emailtohl.getEmail());
		identityService.saveUser(u);
		return u;
	}
	
}
