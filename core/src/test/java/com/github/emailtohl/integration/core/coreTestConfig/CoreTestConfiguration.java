package com.github.emailtohl.integration.core.coreTestConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.github.emailtohl.integration.core.config.CoreConfiguration;

/**
 * 业务层的配置
 * @author HeLei
 */
@Configuration
@Import(CoreConfiguration.class)
public class CoreTestConfiguration {
	/**
	 * 创造测试数据
	 * @return
	 */
	@Bean
	public AppendTestData initEmbeddedDataSource(LocalContainerEntityManagerFactoryBean entityManagerFactory, Environment env) {
		return new AppendTestData(entityManagerFactory.getObject(), env);
	}
}
