package com.github.emailtohl.integration.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

/**
 * 应用上下文的配置
 * @author HeLei
 * @date 2017.06.15
 */
@Configuration
@ComponentScan(basePackages = {
		"com.github.emailtohl.integration.user",
		"com.github.emailtohl.integration.cms",
		"com.github.emailtohl.integration.flow"}, 
excludeFilters = @ComponentScan.Filter({
	Controller.class, Configuration.class }))
@Import(JpaConfiguration.class)
public class ServiceConfiguration {

}
