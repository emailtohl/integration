package com.github.emailtohl.integration.web;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import com.github.emailtohl.integration.common.commonTestConfig.ServiceConfiguration;
import com.github.emailtohl.integration.web.config.ActivitiConfiguration;

/**
 * 对Web层测试的配置
 * @author HeLei
 */
@Configurable
@Import(ActivitiConfiguration.class)
@ComponentScan({"com.github.emailtohl.integration.web.aop", "com.github.emailtohl.integration.web.service"})
@EnableAspectJAutoProxy
public class WebTestConfig {
	
	@Bean
	public WebTestData webTestData() {
		return new WebTestData();
	}
	
}
