package com.github.emailtohl.integration.nuser.userTestConfig;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.util.ErrorHandler;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 业务层的配置
 * @author HeLei
 */
@Configuration
@ComponentScan(basePackages = "com.github.emailtohl.integration.nuser", excludeFilters = @ComponentScan.Filter({
		Controller.class, Configuration.class }))
@EnableCaching
@Import(JpaConfiguration.class)
public class ServiceConfiguration {
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * 简单的缓存管理器的实现
	 * 
	 * @return
	 */
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}
	
	/*@Bean
	public LocalValidatorFactoryBean localValidatorFactoryBean() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		// LocalValidatorFactoryBean会自动在classpath下搜索Bean Validation的实现
		// 我们主要用的实现是HibernateValidator，但若在JAVA EE容器里面有多个提供者就不可预测，故还是手动设置提供类
		validator.setProviderClass(HibernateValidator.class);
		return validator;
	}*/
	
	/**
	 * MethodValidationPostProcessor会寻找标注了@org.springframework.validation.annotation.Validated
	 * 和@javax.validation.executable.ValidateOnExecution的类，并为其创建校验的代理
	 * @return
	 */
	/*@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor(LocalValidatorFactoryBean validator) {
		MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
		processor.setValidator(validator);
		return processor;
	}*/
	
	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		LOG.info("Setting up thread pool task scheduler with 20 threads.");
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(20);
		scheduler.setThreadNamePrefix("task-");
		scheduler.setAwaitTerminationSeconds(60);
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setErrorHandler(new ErrorHandler() {
			@Override
			public void handleError(Throwable t) {
				LOG.error("Unknown error occurred while executing task.", t);
			}
		});
		scheduler.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				LOG.error("Execution of task {} was rejected for unknown reasons.", r);
			}
		});
		return scheduler;
	}

	@Bean
	public Gson gson() {
		return new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				return false;
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				if (clazz == byte[].class) {
					return true;
				}
				return false;
			}
		})/* .setDateFormat(Constant.DATE_FORMAT) */.create();
	}
}
