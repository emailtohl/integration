package com.github.emailtohl.integration.web.webTestConfig;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.emailtohl.integration.web.config.DataSourceConfiguration;

/**
 * JPA Hibernate配置
 * @author HeLei
 * @date 2017.06.12
 */
@Configuration
//启用注解式事务管理，配置类通常要实现TransactionManagementConfigurer接口，确定使用哪个事务管理器
@EnableTransactionManagement
//这是SpringData的注解，启动后，它将扫描指定包中继承了Repository（实际业务代码中的接口是间接继承它）的接口，并为其提供代理
//repositoryImplementationPostfix = "Impl" 扫描实现类的名字，若该类的名字为接口名+"Impl"，则认为该实现类将提供SpringData以外的功能
@EnableJpaRepositories(basePackages = {
		"com.github.emailtohl.integration.user.dao", 
		"com.github.emailtohl.integration.cms.dao",
		"com.github.emailtohl.integration.flow.dao",
		}, 
		repositoryImplementationPostfix = "Impl", 
		transactionManagerRef = "annotationDrivenTransactionManager", 
		entityManagerFactoryRef = "entityManagerFactory")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Import(DataSourceConfiguration.class)
class JpaConfiguration {
	public static final String[] ENTITIES_PACKAGE = {
			"com.github.emailtohl.integration.user.entities", 
			"com.github.emailtohl.integration.cms.entities",
			"com.github.emailtohl.integration.flow.entities",
	};
	
	/*
	hibernate.hbm2ddl.auto参数的作用主要用于：自动创建|更新|验证数据库表结构。如果不是此方面的需求建议set value="none"。
	create：
	每次加载hibernate时都会删除上一次的生成的表，然后根据你的model类再重新来生成新表，哪怕两次没有任何改变也要这样执行，这就是导致数据库表数据丢失的一个重要原因。
	create-drop ：
	每次加载hibernate时根据model类生成表，但是sessionFactory一关闭,表就自动删除。
	update：
	最常用的属性，第一次加载hibernate时根据model类会自动建立起表的结构（前提是先建立好数据库），以后加载hibernate时根据 model类自动更新表结构，即使表结构改变了但表中的行仍然存在不会删除以前的行。要注意的是当部署到服务器后，表结构是不会被马上建立起来的，是要等 应用第一次运行起来后才会。
	validate ：
	每次加载hibernate时，验证创建数据库表结构，只会和数据库中的表进行比较，不会创建新表，但是会插入新值。
	*/
	public static final String hibernate_hbm2ddl_auto = "create-drop";
	
	@Inject
	DataSource dataSource;
	@Inject
	Environment env;
	@Inject
	@Named("indexBase")
	File indexBase;
	
	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}
	
	/**
	 * 使用META-INFO/persistence.xml中的配置
	 * @return
	 */
	@Bean(name = "entityManagerFactory")
	public LocalEntityManagerFactoryBean entityManagerFactory() {
		LocalEntityManagerFactoryBean emfb = new LocalEntityManagerFactoryBean();
		emfb.setPersistenceUnitName("integration-unit");
		return emfb;
	}
	
	@Bean(name = "annotationDrivenTransactionManager")
	public PlatformTransactionManager jpaTransactionManager() {
		return new JpaTransactionManager(entityManagerFactory().getObject());
	}
	
	@Bean
	public DataSourceTransactionManager transactionManagerForTest() {
		return new DataSourceTransactionManager(dataSource);
	}
	
	/**
	 * 创造测试数据
	 * @return
	 */
	@Bean
	public InitDataSource initEmbeddedDataSource() {
		return new InitDataSource(entityManagerFactory().getObject());
	}
	
	/**
	 * 默认情况下，Spring总是使用ID为annotationDrivenTransactionManager的事务管理器
	 * 若实现了TransactionManagementConfigurer接口，则可以自定义提供事务管理器
	 * 注意：如果没有实现接口TransactionManagementConfigurer，且事务管理器的名字不是默认的annotationDrivenTransactionManager，可在注解 @Transactional的value指定。
	 */
	@Bean
	public PersistenceExceptionTranslator persistenceExceptionTranslator() {
		return new HibernateExceptionTranslator();
	}
	
	@Bean(name = "auditorAware")
	public AuditorAware<String> auditorAwareImpl() {
		return new AuditorAware<String>() {
			@Override
			public String getCurrentAuditor() {
				return "tester";
			}
		};
	}
	
	/**
	 * 获取LocalSessionFactoryBuilder，由此可获取Hibernate的SessionFactory
	 * 这里没有直接向spring注册Hibernate的SessionFactory，是因为会影响entityManagerFactory单例
	 * @return
	 */
	@Bean
	public LocalSessionFactoryBuilder sessionFactory() {
		LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
		builder.scanPackages("com.github.emailtohl.integration.common.testData");
		if (contains(DataSourceConfiguration.H2_RAM_DB)) {
			builder.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		} else {
			builder.setProperty("hibernate.dialect", PostgreSQL9Dialect.class.getCanonicalName());
		}
		builder.setProperty("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto);
		// hibernate.search.default.directory_provider默认是filesystem
		// 设置hibernate.search.default.indexBase可指定索引目录
		builder.setProperty("hibernate.search.default.directory_provider", "ram");
		return builder;
	}
	
	/**
	 * 测试当前环境是否含有此名字
	 * @param envName
	 * @return
	 */
	public boolean contains(String envName) {
		for (String s : env.getActiveProfiles()) {
			if (envName.equals(s)) {
				return true;
			}
		}
		return false;
	}
}
