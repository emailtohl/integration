package com.github.emailtohl.integration.web.config;

import org.activiti.rest.common.application.DefaultContentTypeResolver;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * 基于Activiti的Rest接口的配置
 * @author HeLei
 */
@Configuration
@ComponentScan(basePackages = "org.activiti.rest", includeFilters = @ComponentScan.Filter(Controller.class))
public class ActivitiRestConfiguration {

	@Bean
	public RestResponseFactory restResponseFactory() {
		return new RestResponseFactory();
	}
	
	@Bean
	public DefaultContentTypeResolver DefaultContentTypeResolver() {
		return new DefaultContentTypeResolver();
	}
	
	@Bean
	public ObjectMapper ObjectMapper() {
		return new ObjectMapper();
	}
}
