package com.github.emailtohl.integration.web.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.HibernateValidator;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.github.emailtohl.integration.common.lucene.FileSearch;

import freemarker.template.TemplateExceptionHandler;

/**
 * 应用上下文的配置
 * @author HeLei
 * @date 2017.06.15
 */
@Configuration
//启用注解式事务管理，配置类通常要实现TransactionManagementConfigurer接口，确定使用哪个事务管理器
@EnableTransactionManagement
//启动Aspect动态代理
@EnableAspectJAutoProxy
//Bean中注解@Async的方法会异步执行
@EnableAsync
//启动时间计划任务，spring在扫描类时，发现有@Scheduled注解的方法，即可定时执行该方法
@EnableScheduling
//开启缓存功能，当执行到一个被@Cacheable注解的方法时，Spring首先检查condition条件是否满足，如果不满足，执行方法，返回；
//如果满足，在name所命名的缓存空间中查找使用key存储的对象，如果找到，将找到的结果返回，如果没有找到执行方法，将方法的返回值以key-value对象的方式存入name缓存中，然后方法返回。
@EnableCaching
@ComponentScan(basePackages = {
		"com.github.emailtohl.integration.message",
		"com.github.emailtohl.integration.user",
		"com.github.emailtohl.integration.cms",
		"com.github.emailtohl.integration.flow",
		"com.github.emailtohl.integration.web",
	}, 
	excludeFilters = @ComponentScan.Filter({ Controller.class, Configuration.class }))
@Import({JpaConfiguration.class, ThreadConfiguration.class, SecurityConfiguration.class})
public class ServiceConfiguration implements TransactionManagementConfigurer, AsyncConfigurer, SchedulingConfigurer {
	private static final Logger logger = LogManager.getLogger();

	/**
	 * 由JpaConfiguration配置的Bean注入
	 */
	@Inject
	@Named("annotationDrivenTransactionManager")
	PlatformTransactionManager jpaTransactionManager;

	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return jpaTransactionManager;
	}

	/**
	 * 由ThreadConfiguration配置的Bean注入
	 */
	@Inject
	ThreadPoolTaskScheduler taskScheduler;
	/**
	 * 配置任务执行器，所以需要实现SchedulingConfigurer接口
	 */
	@Override
	public void configureTasks(ScheduledTaskRegistrar registrar) {
		logger.info("Configuring scheduled method executor {}.", taskScheduler);
		registrar.setTaskScheduler(taskScheduler);
	}
	/**
	 * 配置异步执行器，所以需要实现AsyncConfigurer
	 * 应用程序中通常使用的是JDK提供的线程，如：
	 * ExecutorService = Executors.newCachedThreadPool();
	 * 不过这会启动额外的资源，为了让整个应用程序启动的线程在可控范围内，可以统一使用注册在Spring中的taskScheduler
	 */
	@Override
	public Executor getAsyncExecutor() {
		logger.info("Configuring asynchronous method executor {}.", taskScheduler);
		return taskScheduler;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new AsyncUncaughtExceptionHandler() {
			@Override
			public void handleUncaughtException(Throwable ex, Method method, Object... params) {
				logger.error("调用异步任务出错了, message : " + method, ex);
			}
		};
	}

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
	
	/**
	 * 简单的缓存管理器的实现
	 * @return
	 */
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}
	
	/**
	 * ApplicationContext的实现类本身也是一个MessageSource对象。
	 * Bean名字为messageSource是有意义的，它将这个Bean定义的信息资源加载为容器级的国际化信息资源。
	 * 
	 * @return
	 */
	@Bean
	public MessageSource messageSource() {
		// 基于Java的ResourceBundle基础类实现，允许仅通过资源名加载国际化资源。
		// ReloadableResourceBundleMessageSource提供了定时刷新功能，允许在不重启系统的情况下，更新资源的信息。
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setCacheSeconds(-1);// 指定时间刷新，默认是-1永不刷新
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
		messageSource.setBasenames(
			"/WEB-INF/i18n/titles", 
			"/WEB-INF/i18n/messages", 
			"/WEB-INF/i18n/errors",
			"/WEB-INF/i18n/validation"
		);
		return messageSource;
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
	
	/**
	 * freemarker的配置
	 * @return
	 * @throws IOException
	 */
	@Bean
	public freemarker.template.Configuration freeMarkerConfiguration(@Named("templatesPath") File templatesPath) throws IOException {
		freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
		cfg.setDirectoryForTemplateLoading(templatesPath);
		cfg.setDefaultEncoding("UTF-8");
		// Sets how errors will appear.
		// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
		cfg.setLogTemplateExceptions(false);
		return cfg;
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
	
	/**
	 * 提供RMI访问服务
	 * @param authenticationService
	 * @return
	 */
	@Bean
	public RmiServiceExporter rmiExporter(@Named("authenticationProviderImpl") AuthenticationProvider authenticationService) {
		RmiServiceExporter rmiExporter = new RmiServiceExporter();
		rmiExporter.setService(authenticationService);
		rmiExporter.setServiceName("authenticationServiceRMI");
		rmiExporter.setServiceInterface(AuthenticationProvider.class);
		rmiExporter.setRegistryPort(1199);
		return rmiExporter;
	}
	
	/**
	 * 访问本RMI服务
	 * @return
	 */
	@Bean
	public RmiProxyFactoryBean authenticationServiceRMI() {
		RmiProxyFactoryBean rmiProxy = new RmiProxyFactoryBean();
		rmiProxy.setServiceUrl("rmi://localhost:1199/authenticationServiceRMI");
		rmiProxy.setServiceInterface(AuthenticationProvider.class);
		return rmiProxy;
	}
}
