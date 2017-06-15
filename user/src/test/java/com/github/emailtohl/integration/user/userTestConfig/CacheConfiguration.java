package com.github.emailtohl.integration.user.userTestConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 对接口缓存的测试
 * @author HeLei
 * @date 2017.06.15
 */
@Configuration
@Import(JpaConfiguration.class)
public class CacheConfiguration {

}
