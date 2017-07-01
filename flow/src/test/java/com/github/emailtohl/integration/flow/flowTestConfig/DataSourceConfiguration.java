package com.github.emailtohl.integration.flow.flowTestConfig;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * 测试的spring上下文配置
 * @author HeLei
 * @date 2017.06.12
 */
@Configuration
@PropertySource({ "classpath:database.properties" })
public class DataSourceConfiguration {
	public static final String DB_CONFIG = "db_config";
	public static final String DB_RAM_H2 = "db_ram_h2";
	
	/**
	 * 静态配置方法，该方法将在最早执行，这样才能读取properties配置
	 * @return
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Value("${jdbc.driverClassName}")
	String driverClassName;
	@Value("${jdbc.url}")
	String url;
	@Value("${jdbc.username}")
	String username;
	@Value("${jdbc.password}")
	String password;
	
	/**
	 * 内存数据库
	 * @return
	 */
	@Profile(DB_RAM_H2)
	@Bean
	public DataSource embeddedDataSource() {
		return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
//				.addScripts("classpath:test-data.sql")
				.build();
	}
	
	@Profile(DB_CONFIG)
	@Bean(name = "test_dataSource")
	public DataSource testDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}
	
}
