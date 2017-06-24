package com.github.emailtohl.integration.web.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
		"com.github.emailtohl.integration.conference.dao",
		}, 
		repositoryImplementationPostfix = "Impl", 
		transactionManagerRef = "annotationDrivenTransactionManager", 
		entityManagerFactoryRef = "entityManagerFactory")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Import(DataSourceConfiguration.class)
public class JpaConfiguration {
	public static final String[] ENTITIES_PACKAGE = {
			"com.github.emailtohl.integration.user.entities", 
			"com.github.emailtohl.integration.cms.entities",
			"com.github.emailtohl.integration.flow.entities",
			"com.github.emailtohl.integration.conference.entities",
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
	public static final String hibernate_hbm2ddl_auto = "update";
	
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
	
	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
		if (contains(DataSourceConfiguration.H2_RAM_DB)) {
			adapter.setDatabase(Database.H2);
			adapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");
		} else {
			adapter.setDatabase(Database.POSTGRESQL);
			adapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQL94Dialect");
		}
		adapter.setShowSql(false);
		adapter.setGenerateDdl(false);
		return adapter;
	}
	
	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
		emfb.setDataSource(dataSource);
		emfb.setJpaVendorAdapter(jpaVendorAdapter());
		// 实际上hibernate可以扫描类路径下有JPA注解的实体类，但是JPA规范并没有此功能，所以最好还是告诉它实际所在位置
		emfb.setPackagesToScan(ENTITIES_PACKAGE);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto);
		properties.put("hibernate.format_sql", "true");
		// hibernate.search.default.directory_provider默认是filesystem
		// 设置hibernate.search.default.indexBase可指定索引目录
		properties.put("hibernate.search.default.indexBase", indexBase.getAbsolutePath());
		emfb.setJpaPropertyMap(properties);
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
				String s = "";
				SecurityContext c = SecurityContextHolder.getContext();
				if (c != null) {
					Authentication a = c.getAuthentication();
					if (a != null) {
						s = a.getName();
					}
				}
				return s;
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
		builder.setProperty("hibernate.search.default.directory_provider", "filesystem");
		builder.setProperty("hibernate.search.default.indexBase", indexBase.getAbsolutePath());
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
