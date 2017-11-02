package com.github.emailtohl.integration.nuser.userTestConfig;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 业务层的配置
 * 
 * @author HeLei
 * @date 2017.06.15
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
