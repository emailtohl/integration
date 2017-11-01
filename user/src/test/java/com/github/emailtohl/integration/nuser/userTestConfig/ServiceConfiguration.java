package com.github.emailtohl.integration.nuser.userTestConfig;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

import com.github.emailtohl.integration.common.Constant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 业务层的配置
 * @author HeLei
 * @date 2017.06.15
 */
@Configuration
@ComponentScan(basePackages = "com.github.emailtohl.integration.nuser", excludeFilters = @ComponentScan.Filter({
	Controller.class, Configuration.class }))
@EnableCaching
@Import(JpaConfiguration.class)
public class ServiceConfiguration {
	/**
	 * 简单的缓存管理器的实现
	 * @return
	 */
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}
	
	@Bean
	public Gson gson() {
		return new GsonBuilder().setDateFormat(Constant.DATE_FORMAT).create();
	}
}
