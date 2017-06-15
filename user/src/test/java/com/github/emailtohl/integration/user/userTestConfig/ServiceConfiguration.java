package com.github.emailtohl.integration.user.userTestConfig;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

/**
 * 业务层的配置
 * @author HeLei
 * @date 2017.06.15
 */
@Configuration
@ComponentScan(basePackages = "com.github.emailtohl.integration.user", excludeFilters = @ComponentScan.Filter({
	Controller.class, Configuration.class }))
@Import(JpaConfiguration.class)
public class ServiceConfiguration {

}
