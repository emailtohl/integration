package com.github.emailtohl.integration.nuser.auth;

import javax.inject.Named;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import com.github.emailtohl.integration.nuser.userTestConfig.ServiceConfiguration;

/**
 * 对接口安全权限的测试
 * @author HeLei
 */
@Configuration
// 为了在应用层而非过滤器中使用spring security，还可以启动@EnableGlobalMethodSecurity功能
// 这时候spring会在调用Bean方法时再添加一个切面，执行spring security的安全检查
@EnableGlobalMethodSecurity(prePostEnabled = true, order = 0, mode = AdviceMode.PROXY, proxyTargetClass = false)
@Import(ServiceConfiguration.class)
class SecurityConfiguration {
	/*
	@Bean
	public AuthenticationProvider authenticationProvider() {
		
	}*/
	
	// AuthenticationManager来源于com.github.emailtohl.integration.user.auth
	@Bean
	public SecurityContextManager securityContextManager(@Named("authenticationManager") AuthenticationManager authenticationManager) {
		return new SecurityContextManager(authenticationManager);
	}
}

