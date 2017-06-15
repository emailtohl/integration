package com.github.emailtohl.integration.user.userTestConfig;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 对接口缓存的测试
 * @author HeLei
 * @date 2017.06.15
 */
@Configuration
//开启缓存功能，当执行到一个被@Cacheable注解的方法时，Spring首先检查condition条件是否满足，如果不满足，执行方法，返回；
//如果满足，在name所命名的缓存空间中查找使用key存储的对象，如果找到，将找到的结果返回，如果没有找到执行方法，将方法的返回值以key-value对象的方式存入name缓存中，然后方法返回。
@EnableCaching
@Import(ServiceConfiguration.class)
public class CacheConfiguration {
	/**
	 * 简单的缓存管理器的实现
	 * @return
	 */
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}

}
