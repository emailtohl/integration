package com.github.emailtohl.integration.web.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.github.emailtohl.integration.core.config.CoreConfiguration;
/**
 * 使用 MyBatis-Spring 的主要原因是它允许 MyBatis 参与到 Spring 的事务管理中。而不是给 MyBatis 创建一个新的特定的事务管理器。
 * Mybatis的基本使用是先在SqlSessionFactory中获取非线程安全的SqlSession，后再使用SqlSession的getMapper获取数据访问的Mapper接口
 * 
 * 而SqlSessionTemplate的配置，使得可以将SqlSession直接注入到业务Bean中，通过代理使得SqlSession线程安全。
 * 
 * 然而同样会使用许多SqlSession.getMapper重复代码，所以MapperScannerConfigurer的配置可将Mapper接口注入到业务Bean中。
 * 
 * 需要注意的是，Mybatis初始化时并没有将Mapper.xml的命名空间与Mapper接口关联起来，而是在调用Mapper接口时再在Mapper.xml配置中查询配置。
 * 
 * @author HeLei
 */
@Configuration
@Import(CoreConfiguration.class)
class MybatisConfiguration {
	/**
	 * 一个数据源对应一个SqlSessionFactory
	 * mybatis的session工厂，关于事务方面，有下面的这段说明:
	 * Spring will automatically use any existing container transaction and attach
	 * an SqlSession to it. If no transaction is started and one is needed based on
	 * the transaction configuration, Spring will start a new container managed
	 * transaction.
	 * 上下文中配置了两个事务管理器：JpaTransactionManager和DataSourceTransactionManager
	 * 他们都实现了PlatformTransactionManager接口，但是在CoreConfiguration配置中，指定了默认的任务管理器是JpaTransactionManager
	 * 所以，Mybatis应该是使用的JpaTransactionManager，这样就和JPA事务统一起来。
	 * 
	 * @param dataSource
	 * @return
	 * @throws Exception
	 */
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
//		Environment env = new Environment("web", new JdbcTransactionFactory(), dataSource);
//		org.apache.ibatis.session.Configuration cfg = new org.apache.ibatis.session.Configuration();
//		cfg.setEnvironment(env);
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		// 配置类型别名
		sessionFactory.setTypeAliasesPackage("com.github.emailtohl.integration.core.user.entities");
		// 配置mapper的扫描，找到所有的mapper.xml映射文件
		Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*Mapper.xml");
		sessionFactory.setMapperLocations(resources);
//		sessionFactory.setTransactionFactory(new JdbcTransactionFactory());
		return sessionFactory.getObject();
	}
	
	/**
	 * 线程安全的mybatis session，可直接注入到Service中
	 * @param sqlSessionFactory
	 * @return
	 */
	@Bean
	public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
	
	/**
	 * 不再为每个Bean注入SqlSession接口，扫描包中的映射器 Mapper接口，并自动地注册为Bean
	 * 也可以使用@MapperScan 注解
	 * @return
	 */
	@Bean
	public MapperScannerConfigurer mapperScannerConfigurer() {
		MapperScannerConfigurer cfg = new MapperScannerConfigurer();
		cfg.setBasePackage("com.github.emailtohl.integration.web");
		// 只扫描带有@MybatisMapperInterface注解的接口
		cfg.setAnnotationClass(MybatisMapperInterface.class);
		cfg.setSqlSessionFactoryBeanName("sqlSessionFactory");
		return cfg;
	}
}
