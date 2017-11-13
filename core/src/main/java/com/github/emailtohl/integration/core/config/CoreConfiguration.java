package com.github.emailtohl.integration.core.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.util.ErrorHandler;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.github.emailtohl.integration.common.lucene.FileSearch;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 业务层的配置
 * @author HeLei
 */
@Configuration
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = "com.github.emailtohl.integration.core", excludeFilters = @ComponentScan.Filter({
		Controller.class, Configuration.class }))
@EnableCaching
@Import(JpaConfiguration.class)
public class CoreConfiguration implements TransactionManagementConfigurer, AsyncConfigurer, SchedulingConfigurer {
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
	
	/**
	 * LocalValidatorFactoryBean同时支持javax.validation.Validator和org.springframework.validation.Validator两个接口
	 * 前者是Java EE规范的一个校验接口，后者是前者的门面，它不仅提供统一的报错机制，还可以应用于Spring MVC的验证中
	 * @return
	 */
	@Bean
	public LocalValidatorFactoryBean localValidatorFactoryBean() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		// LocalValidatorFactoryBean会自动在classpath下搜索Bean Validation的实现
		// 我们主要用的实现是HibernateValidator，但若在JAVA EE容器里面有多个提供者就不可预测，故还是手动设置提供类
		validator.setProviderClass(HibernateValidator.class);
		return validator;
	}
	
	/**
	 * MethodValidationPostProcessor会寻找标注了@org.springframework.validation.annotation.Validated
	 * 和@javax.validation.executable.ValidateOnExecution的类，并为其创建校验的代理
	 * 需要<groupId>javax.el</groupId><artifactId>javax.el-api</artifactId>和
	 * <groupId>org.glassfish.web</groupId><artifactId>el-impl</artifactId>包支持
	 * 
	 * @return
	 */
	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor(LocalValidatorFactoryBean validator) {
		MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
		processor.setValidator(validator);
		return processor;
	}
	
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

	/**
	 * 让任务管理器共享同一个线程执行器
	 */
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setTaskScheduler(taskScheduler());
	}

	/**
	 * 让异步执行器共享同一个线程执行器
	 */
	@Override
	public Executor getAsyncExecutor() {
		return taskScheduler();
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (Throwable ex, Method method, Object... params) -> LOG.error("调用异步任务出错了, message : " + method, ex);
	}

	/**
	 * 默认情况下，Spring总是使用ID为annotationDrivenTransactionManager的事务管理器
	 * 若实现了TransactionManagementConfigurer接口，则可以自定义提供事务管理器
	 * 注意：如果没有实现接口TransactionManagementConfigurer，且事务管理器的名字不是默认的annotationDrivenTransactionManager，可在注解 @Transactional的value指定。
	 */
	@Inject
	PlatformTransactionManager jpaTransactionManager;
	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return jpaTransactionManager;
	}
	
	/**
	 * 文件系统搜索组件
	 * @return
	 * @throws IOException
	 */
	@Bean
	public FileSearch fileSearch(@Named("indexBase") File indexBase) throws IOException {
		File indexDir = new File(indexBase, FileSearch.class.getName());
		if (!indexDir.exists()) {
			indexDir.mkdir();
		}
		FileSearch fileSearch = new FileSearch(indexDir.getAbsolutePath());
		return fileSearch;
	}
	
	@Inject
	Environment env;
	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setDefaultEncoding("UTF-8");
		mailSender.setHost(env.getProperty("mailserver.host"));
		mailSender.setPort(Integer.valueOf(env.getProperty("mailserver.port")));
		mailSender.setUsername(env.getProperty("mailserver.username"));
		mailSender.setPassword(env.getProperty("mailserver.password"));
		Properties p = new Properties();
		p.setProperty("mail.debug", "true");
		String proxyHost = env.getProperty("proxyHost");
		String proxyPort = env.getProperty("proxyPort");
		String auth = env.getProperty("mailserver.auth");
		
		// 暂未找到通过代理发送邮件的方法
		if (proxyHost != null && !proxyHost.isEmpty()) {
		}
		if (proxyPort != null && !proxyPort.isEmpty()) {
		}
		if (auth != null && !auth.isEmpty()) {
			p.setProperty("mail.smtp.auth", auth);
		}
		mailSender.setJavaMailProperties(p);
		return mailSender;
	}
}
