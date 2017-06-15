package com.github.emailtohl.integration.user.userTestConfig;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * 对接口安全权限的测试
 * @author HeLei
 * @date 2017.06.15
 */
@Configuration
// 为了在应用层而非过滤器中使用spring security，还可以启动@EnableGlobalMethodSecurity功能
// 这时候spring会在调用Bean方法时再添加一个切面，执行spring security的安全检查
@EnableGlobalMethodSecurity(prePostEnabled = true, order = 0, mode = AdviceMode.PROXY, proxyTargetClass = false)
@Import({/*ServiceConfiguration.class, */CacheConfiguration.class})// 在缓存策略上，可以提高安全测试的性能
public class SecurityConfiguration {
	
}
