package com.github.emailtohl.integration.core.config;

import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

/**
 * 测试的spring上下文配置
 * @author HeLei
 */
@Configuration
//启用注解式事务管理，配置类通常要实现TransactionManagementConfigurer接口，确定使用哪个事务管理器
@EnableTransactionManagement
//这是SpringData的注解，启动后，它将扫描指定包中继承了Repository（实际业务代码中的接口是间接继承它）的接口，并为其提供代理
//repositoryImplementationPostfix = "Impl" 扫描实现类的名字，若该类的名字为接口名+"Impl"，则认为该实现类将提供SpringData以外的功能
@EnableJpaRepositories(basePackages = "com.github.emailtohl.integration", 
		repositoryImplementationPostfix = "Impl", 
		transactionManagerRef = "annotationDrivenTransactionManager", 
		entityManagerFactoryRef = "entityManagerFactory")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Import(DataSourceConfiguration.class)
class JpaConfiguration {
	public static final String[] ENTITIES_PACKAGE = {"com.github.emailtohl.integration"};
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
		if (contains(DB_RAM_H2)) {
			adapter.setDatabase(Database.H2);
			adapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");
		} else {
			String driverClassName = env.getProperty("jdbc.driverClassName");
			if (driverClassName.contains("postgresql")) {
				adapter.setDatabase(Database.POSTGRESQL);
				adapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQL94Dialect");
			} else if (driverClassName.contains("mysql")) {
				adapter.setDatabase(Database.MYSQL);
				adapter.setDatabasePlatform("org.hibernate.dialect.MySQL57InnoDBDialect");
			} else {
				throw new IllegalArgumentException("仅支持postgresql和mysql");
			}
		}
		adapter.setShowSql(false);
		adapter.setGenerateDdl(false);
		return adapter;
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
		emfb.setDataSource(dataSource);
		emfb.setJpaVendorAdapter(jpaVendorAdapter());
		// 实际上hibernate可以扫描类路径下有JPA注解的实体类，但是JPA规范并没有此功能，所以最好还是告诉它实际所在位置
		emfb.setPackagesToScan(ENTITIES_PACKAGE);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto);
		// 关于sql的打印
		if (showSql()) {
			properties.put("hibernate.show_sql", "true");
			properties.put("hibernate.format_sql", "true");
//			properties.put("hibernate.use_sql_comments", "true");
		}
		// hibernate.search.default.directory_provider默认是filesystem
		// 设置hibernate.search.default.indexBase可指定索引目录
		if (contains(DB_RAM_H2)) // 使用内存数据库一般是测试环境，可以使用内存来做索引的存储空间
			properties.put("hibernate.search.default.directory_provider", "local-heap");
		else
			properties.put("hibernate.search.default.indexBase", indexBase.getAbsolutePath());
		emfb.setJpaPropertyMap(properties);
		return emfb;
	}
	
	@Bean(name = "annotationDrivenTransactionManager")
	public PlatformTransactionManager jpaTransactionManager() {
		return new JpaTransactionManager(entityManagerFactory().getObject());
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
		// getCurrentAuditor
		return () -> {
			String name = "anonymous";
			Authentication a = SecurityContextHolder.getContext().getAuthentication();
			if (a != null) {
				name = a.getName();
			}
			return Optional.<String>of(name);
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
		builder.scanPackages(ENTITIES_PACKAGE);
		if (contains(DB_RAM_H2)) {
			builder.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		} else {
			builder.setProperty("hibernate.dialect", PostgreSQL9Dialect.class.getCanonicalName());
		}
		builder.setProperty("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto);
		// hibernate.search.default.directory_provider默认是filesystem
		// 设置hibernate.search.default.indexBase可指定索引目录
		if (contains(DB_RAM_H2)) // 使用内存数据库一般是测试环境，可以使用内存来做索引的存储空间
			builder.setProperty("hibernate.search.default.directory_provider", "ram");
		else
			builder.setProperty("hibernate.search.default.indexBase", indexBase.getAbsolutePath());
		return builder;
	}
	
	/**
	 * 测试当前环境是否含有此名字
	 * @param envName
	 * @return
	 */
	public boolean contains(String envName) {
		return Arrays.stream(env.getActiveProfiles()).anyMatch(s -> envName.equals(s));
	}
	
	/**
	 * 根据在日志文件中hibernate参数的级别，确定是否显示sql
	 * @return
	 */
	private boolean showSql() {
		Document d = null;
		try {// 首先查看测试环境
			Resource r = new ClassPathResource("log4j2-test.xml");
			d = Jsoup.parse(r.getFile(), "UTF-8");
		} catch (IOException e) {}
		if (d == null) {
			try {// 再查看正式环境
				Resource r = new ClassPathResource("log4j2.xml");
				d = Jsoup.parse(r.getFile(), "UTF-8");
			} catch (IOException e) {}
		}
		if (d == null) {// 若没查找到，直接返回
			return false;
		}
		Element e = d.selectFirst("logger[name=\"org.hibernate.type.descriptor.sql\"]");
		if (e == null) {// 若没查找到该元素，则返回
			return false;
		}
		String level = e.attr("level");
		if (!StringUtils.hasText(level)) {// 若没查找到该属性，则返回
			return false;
		}
		if ("TRACE".equals(level.toUpperCase())) {// 若是TRACE级别的，则hibernate会打印参数，返回true
			return true;
		}
		return false;
	}
}

