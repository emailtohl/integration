package com.github.emailtohl.integration.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

/**
 * 引导CoreConfiguration
 * 
 * @author HeLei
 */
@Configuration
//启动Aspect动态代理
@EnableAspectJAutoProxy
@Import({ PresetDataConfiguration.class, SecurityConfiguration.class })
@ComponentScan(basePackages = "com.github.emailtohl.integration.web", excludeFilters = @ComponentScan.Filter({
	Controller.class, Configuration.class }))
public class ServiceConfiguration {
	
}
