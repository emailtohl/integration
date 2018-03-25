package com.github.emailtohl.integration.web.config;

import org.activiti.rest.common.application.DefaultContentTypeResolver;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * 基于Activiti的Rest接口的配置
 * @author HeLei
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"org.activiti.rest", "org.activiti.conf"})
public class ActivitiRestConfiguration {

	/**
	 * 集成REST服务需要的bean
	 * @return
	 */
	@Bean
	public RestResponseFactory restResponseFactory() {
		return new RestResponseFactory();
	}
	
	@Bean
	public DefaultContentTypeResolver contentTypeResolver() {
		return new DefaultContentTypeResolver();
	}
	
	@Bean
	public ObjectMapper ObjectMapper() {
		return new ObjectMapper();
	}
	
}
