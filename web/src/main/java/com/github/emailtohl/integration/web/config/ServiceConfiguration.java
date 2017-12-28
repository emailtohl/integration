package com.github.emailtohl.integration.web.config;

import java.io.File;
import java.io.IOException;

import javax.inject.Named;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

import freemarker.template.TemplateExceptionHandler;

/**
 * 引导CoreConfiguration
 * 
 * @author HeLei
 */
@Configuration
//启动Aspect动态代理
@EnableAspectJAutoProxy
@Import({ ActivitiConfiguration.class, SecurityConfiguration.class })
@ComponentScan(basePackages = "com.github.emailtohl.integration.web", excludeFilters = @ComponentScan.Filter({
	Controller.class, Configuration.class }))
public class ServiceConfiguration {
	/**
	 * freemarker的配置
	 * @return
	 * @throws IOException
	 */
	@Bean
	public freemarker.template.Configuration freeMarkerConfiguration(@Named("templatesPath") File templatesPath) throws IOException {
		freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
		cfg.setDirectoryForTemplateLoading(templatesPath);
		cfg.setDefaultEncoding("UTF-8");
		// Sets how errors will appear.
		// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		// Don't log exceptions inside FreeMarker that it will thrown at you anyway:
		cfg.setLogTemplateExceptions(false);
		return cfg;
	}
}
