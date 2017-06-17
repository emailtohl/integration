package com.github.emailtohl.integration.web.config;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

/**
 * 将线程池配置移到此处，并让ServiceConfiguration导入
 * 这样可以让ServiceConfiguration实现 AsyncConfigurer, SchedulingConfigurer
 * @author HeLei
 * @date 2017.06.17
 */
@Configuration
class ThreadConfiguration {
	private static final Logger logger = LogManager.getLogger();
	/**
	 * Spring的ThreadPoolTaskScheduler既实现了TaskExecutor接口（对@Async注解的方法异步执行），又实现了TaskScheduler接口（对@Scheduled注解方法按计划执行）
	 * 为了让线程资源有效地被管理使用，这里配置的ThreadPoolTaskScheduler不仅为应用程序使用，同时也将其配置到AsyncConfigurer, SchedulingConfigurer接口中。
	 * 这样执行器和调度器都使用相同的线程池。
	 */
	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		logger.info("Setting up thread pool task scheduler with 20 threads.");
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(20);
		scheduler.setThreadNamePrefix("task-");
		scheduler.setAwaitTerminationSeconds(60);
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setErrorHandler(new ErrorHandler() {
			@Override
			public void handleError(Throwable t) {
				logger.error("Unknown error occurred while executing task.", t);
			}
		});
		scheduler.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				logger.error("Execution of task {} was rejected for unknown reasons.", r);
			}
		});
		return scheduler;
	}
}
