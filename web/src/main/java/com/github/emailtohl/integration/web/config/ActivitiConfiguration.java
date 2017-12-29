package com.github.emailtohl.integration.web.config;

import javax.inject.Named;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.github.emailtohl.integration.core.config.CoreConfiguration;

/**
 * 流程配置
 * 
 * @author HeLei
 */
@Configuration
@Import(CoreConfiguration.class)
public class ActivitiConfiguration {
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * 可以集成到Spring管理的事务中
	 * 
	 * @return
	 */
	@Bean
	public SpringProcessEngineConfiguration processEngineConfiguration(DataSource dataSource,
			@Named("annotationDrivenTransactionManager") PlatformTransactionManager platformTransactionManager, 
			LocalContainerEntityManagerFactoryBean jpaEntityManagerFactory) {
		SpringProcessEngineConfiguration cfg = new SpringProcessEngineConfiguration();
		cfg.setDataSource(dataSource);
		cfg.setTransactionManager(platformTransactionManager);
		// 持久化单元名字默认路径在META-INF/persistence.xml，该属性与jpaEntityManagerFactory选择一个即可
		// c.setJpaPersistenceUnitName("default");
		// 实现javax.persistence.EntityManagerFactory的Bean引用，该属性与jpaPersistenceUnitName选择一个即可
		cfg.setJpaEntityManagerFactory(jpaEntityManagerFactory);
		// 是否由Activiti引擎来管理事务，由于在JPA环境中由Spring容器进行统一事务管理，所以此处关闭Activiti引擎对事务的管理
		cfg.setJpaHandleTransaction(false);
		// 表示Activiti引擎是否应该关闭从jpaEntityManagerFactory获取的EntityManager实例
		// 和jpaHandleTransaction类似，由于统一交给Spring容器管理，这里就不需要Activiti引擎再处理了
		cfg.setJpaCloseEntityManager(false);
		
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
	public ApplicationListener<ContextClosedEvent> closeProcessEngine(ProcessEngine engine) {
		return event -> {
			LOG.info("close ProcessEngine");
			engine.close();
		};
	}
}
