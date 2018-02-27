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
 * 使用 MyBatis-Spring 的主要原因是它允许 MyBatis 参与到 Spring 的事务管理中。
 * 而不是给 MyBatis 创建一个新的特定的事务管理器,MyBatis-Spring 利用了存在于 Spring 中的
 * DataSourceTransactionManager。
 * 
 * 一旦 Spring 的 PlatformTransactionManager 配置好了,你可以在 Spring 中以你通常的做法来配置事务。
 * 注：PlatformTransactionManager 在CoreConfiguration中配置为jpaTransactionManager
 * @author HeLei
 */
@Configuration
@Import(CoreConfiguration.class)
public class MybatisConfiguration {
	/* 注册在CoreConfiguration中
	@Bean
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}
	*/
	
	/**
	 * Spring will automatically use any existing container transaction and attach
	 * an SqlSession to it. If no transaction is started and one is needed based on
	 * the transaction configuration, Spring will start a new container managed
	 * transaction.
	 * 
	 * @param dataSource
	 * @return
	 * @throws Exception
	 */
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
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
	
	@Bean
	public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
	
	/**
	 * 不再为每个Bean注入SqlSession接口，扫描包中的映射器 Mapper接口 ，并自动地注册为Bean
	 * 也可以使用@MapperScan 注解
	 * @return
	 */
	@Bean
	public MapperScannerConfigurer mapperScannerConfigurer() {
		MapperScannerConfigurer cfg = new MapperScannerConfigurer();
		cfg.setBasePackage("com.github.emailtohl.integration.web.service");
		return cfg;
	}
}
