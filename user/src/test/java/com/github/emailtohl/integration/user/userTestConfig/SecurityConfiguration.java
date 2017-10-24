package com.github.emailtohl.integration.user.userTestConfig;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.github.emailtohl.integration.common.exception.NotFoundException;
import com.github.emailtohl.integration.user.UserTestData;
import com.github.emailtohl.integration.user.dao.UserRepository;
import com.github.emailtohl.integration.user.security.AuthenticationManagerImpl;
import com.github.emailtohl.integration.user.security.AuthenticationProviderImpl;
import com.github.emailtohl.integration.user.security.UserDetailsServiceImpl;
import com.github.emailtohl.integration.user.service.SecurityContextManager;
import com.github.emailtohl.integration.user.service.UserService;

/**
 * 对接口安全权限的测试
 * @author HeLei
 * @date 2017.06.15
 */
@Configuration
// 为了在应用层而非过滤器中使用spring security，还可以启动@EnableGlobalMethodSecurity功能
// 这时候spring会在调用Bean方法时再添加一个切面，执行spring security的安全检查
@EnableGlobalMethodSecurity(prePostEnabled = true, order = 0, mode = AdviceMode.PROXY, proxyTargetClass = false)
public class SecurityConfiguration {
	
	@Bean
	public UserRepository userRepositoryMock() throws NotFoundException {
		UserRepository userRepository = mock(UserRepository.class,
				withSettings().defaultAnswer(RETURNS_SMART_NULLS).name("cool mockie"));
		UserTestData td = new UserTestData();
		when(userRepository.findByEmail(td.emailtohl.getEmail())).thenReturn(td.emailtohl);
		when(userRepository.findByEmail(td.foo.getEmail())).thenReturn(td.foo);
		when(userRepository.findByEmail(td.bar.getEmail())).thenReturn(td.bar);
		when(userRepository.findByEmail(td.baz.getEmail())).thenReturn(td.baz);
		when(userRepository.findByEmail(td.qux.getEmail())).thenReturn(td.qux);
		return userRepository;
	}
	
	@Bean
	public UserService userServiceMock() throws NotFoundException {
		UserService userService = mock(UserService.class,
				withSettings().defaultAnswer(RETURNS_SMART_NULLS).name("cool mockie"));
		UserTestData td = new UserTestData();
		when(userService.getUserByEmail(td.emailtohl.getEmail())).thenReturn(td.emailtohl);
		when(userService.getUserByEmail(td.foo.getEmail())).thenReturn(td.foo);
		when(userService.getUserByEmail(td.bar.getEmail())).thenReturn(td.bar);
		when(userService.getUserByEmail(td.baz.getEmail())).thenReturn(td.baz);
		when(userService.getUserByEmail(td.qux.getEmail())).thenReturn(td.qux);
		when(userService.getUser(anyLong())).thenReturn(td.baz);
		return userService;
	}

	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return new UserDetailsServiceImpl(userRepository);
	}
	
	@Bean
	public AuthenticationManager authenticationManager(UserRepository userRepository) {
		return new AuthenticationManagerImpl(userRepository);
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider(UserRepository userRepository, UserDetailsService userDetailsService) throws Exception {
		AuthenticationProvider authenticationProvider = new AuthenticationProviderImpl(userRepository);
		return authenticationProvider;
	}
	
	@Bean
	public SecurityContextManager securityContextManager(AuthenticationManager authenticationManager) {
		return new SecurityContextManager(authenticationManager);
	}
	
}
