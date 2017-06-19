package com.github.emailtohl.integration.web.controller;

import org.hibernate.validator.HibernateValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.github.emailtohl.integration.web.config.ThreadConfiguration;

@Configuration
@Import(ThreadConfiguration.class)
class WebConfiguration {
	/**
	 * Spring可以为其管理的Bean提供统一校验的功能，遵循Java EE的Bean Validation规范
	 * 首先，在存放数据的POJOs对象（如JavaBeans-like，实体（entities）或表单（form））中注解校验的内容，
	 * 然后在spring管理的bean中，指明哪些方法参数、哪些Field字段需要校验。
	 * 
	 * 要让Spring具有校验能力，首先得找到校验器工厂，然后再从工厂中获取校验器
	 * Spring自带了一个LocalValidatorFactoryBean校验器工厂
	 * 它可同时支持javax.validation.Validator和org.springframework.validation.Validator两个接口
	 * 前者是Java EE规范的一个校验接口，后者是前者的门面，它不仅提供统一的报错机制，还可以应用于Spring MVC的验证中。
	 * @return
	 */
	@Bean
	public LocalValidatorFactoryBean localValidatorFactoryBean() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		/*
		 * LocalValidatorFactoryBean会自动在classpath下搜索Bean Validation的实现
		 * 我们主要用的实现是HibernateValidator，但若在JAVA EE容器里面有多个提供者就不可预测，故还是手动设置提供类
		 */
		validator.setProviderClass(HibernateValidator.class);
		return validator;
	}
	
	/**
	 * 从校验工厂中获取到真正的校验器
	 * MethodValidationPostProcessor会寻找标注了@org.springframework.validation.annotation.Validated
	 * 和@javax.validation.executable.ValidateOnExecution的类，并为其创建代理
	 */
	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor() {
		MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
		processor.setValidator(this.localValidatorFactoryBean());
		return processor;
	}
}
