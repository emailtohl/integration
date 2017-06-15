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
	/*
	@Inject
	AuthenticationManagerBuilder builder;
	
	@Bean
	public UserService userServiceMock() throws ResourceNotFoundException {
		UserService userService = mock(UserService.class,
				withSettings().defaultAnswer(RETURNS_SMART_NULLS).name("cool mockie"));
		UserTestData td = new UserTestData();
		when(userService.getUserByEmail(td.emailtohl.getEmail())).thenReturn(td.emailtohl);
		when(userService.getUserByEmail(td.foo.getEmail())).thenReturn(td.foo);
		when(userService.getUserByEmail(td.bar.getEmail())).thenReturn(td.bar);
		when(userService.getUserByEmail(td.baz.getEmail())).thenReturn(td.baz);
		when(userService.getUserByEmail(td.qux.getEmail())).thenReturn(td.qux);
		when(userService.getUserByEmail("aaa@test.com")).thenThrow(new ResourceNotFoundException("未找到"));
		String password = "123456";
		when(userService.authenticate(td.emailtohl.getEmail(), password)).thenReturn(td.emailtohl.getAuthentication());
		when(userService.authenticate(td.foo.getEmail(), password)).thenReturn(td.foo.getAuthentication());
		when(userService.authenticate(td.bar.getEmail(), password)).thenReturn(td.bar.getAuthentication());
		when(userService.authenticate(td.baz.getEmail(), password)).thenReturn(td.baz.getAuthentication());
		when(userService.authenticate(td.qux.getEmail(), password)).thenReturn(td.qux.getAuthentication());
		return userService;
	}

	@Bean
	public UserDetailsService userDetailsService(UserService userService) {
		return new UserDetailsServiceImpl(userService);
	}
	
	@Bean
	public AuthenticationManager authenticationManager(UserService userService) {
		return new AuthenticationManagerImpl(userService);
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider(UserService userService, UserDetailsService userDetailsService) throws Exception {
		AuthenticationProvider authenticationProvider = new AuthenticationProviderImpl(userService);
		builder.authenticationProvider(authenticationProvider).userDetailsService(userDetailsService);
		return authenticationProvider;
	}
	
	@Bean
	public SecurityContextManager securityContextManager(AuthenticationManager authenticationManager) {
		return new SecurityContextManager(authenticationManager);
	}
	*/
}
