package com.github.emailtohl.integration.web.boot;

import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import com.github.emailtohl.integration.web.config.DataSourceConfiguration;
import com.github.emailtohl.integration.web.config.ServiceConfiguration;
import com.github.emailtohl.integration.web.config.WebConfiguration;
import com.github.emailtohl.integration.web.filter.PreSecurityLoggingFilter;
import com.github.emailtohl.integration.web.filter.UserPasswordEncryptionFilter;
import com.github.emailtohl.integration.web.listener.SessionListener;
/**
 * 初始化容器时最先启动的类，它将完成如下工作：
 * （1）激活容器默认的servlet来响应静态资源；
 * （2）启动spring的容器
 * （3）注册springmvc
 * （4）其他监听器和过滤器
 * 
 * @author HeLei
 * @date 2017.02.04
 */
@Order(1)
public class ContainerBootstrap implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext container) throws ServletException {
		/* 默认的Servlet可以处理静态资源 */
		container.getServletRegistration("default").addMapping("/site/**", "/common/**", "/lib/*", "resources/**", "/download/*",
				"/upload/*", "/templates/*", "*.html", "*.css", "*.js", "*.png", "*.gif", "*.jpg");

		/* 配置Spring根应用上下文 */
		AnnotationConfigWebApplicationContext serviceContext = new AnnotationConfigWebApplicationContext();
		/* 载入配置 */
		serviceContext.getEnvironment().setActiveProfiles(
			DataSourceConfiguration.JNDI_POSTGRESQL_DB,
			DataSourceConfiguration.ENV_SERVLET
		);// 激活spring配置中的profile
		serviceContext.register(ServiceConfiguration.class);
		container.addListener(new ContextLoaderListener(serviceContext));

		/*
		 * 配置SpringMvc org.springframework.web.context.ContextLoaderListener.
		 * ContextLoaderListener 可以自动将根应用上下文设置为DispatcherServlet的父上下文
		 */
		AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
		/* 载入配置 */
		webContext.register(WebConfiguration.class);
		ServletRegistration.Dynamic dispatcher = container.addServlet("springDispatcher",
				new DispatcherServlet(webContext));
		dispatcher.setLoadOnStartup(1);
		/* 可以上传文件 */
		dispatcher.setMultipartConfig(new MultipartConfigElement(null, 20_971_520L, 41_943_040L, 512_000));
		dispatcher.addMapping("/");
		// 另一种激活spring配置中的profile的方式
//		dispatcher.setInitParameter("spring.profiles.active", DataSourceConfiguration.JNDI_POSTGRESQL_DB);
//		container.setInitParameter("spring.profiles.active", DataSourceConfiguration.ENV_SERVLET);

		/* 在Servlet容器中注册监听器 */
		container.addListener(SessionListener.class);
		
		/* 在Servlet容器中注册过滤器 */
		FilterRegistration.Dynamic registration = container.addFilter("characterEncodingFilter", new CharacterEncodingFilter("UTF-8"));
		// 第一个参数为null：响应默认的DispatcherType.REQUEST
		// 第二个参数为false，表明该filter将在web.xml中配置的任何filter之前 第三个参数表明将响应所有地址
		registration.addMappingForUrlPatterns(null, false, "/*");
		registration = container.addFilter("loggingFilter", new PreSecurityLoggingFilter());
		registration.addMappingForUrlPatterns(null, false, "/*");
		
		// 自定义RSA解密用户密码
		registration = container.addFilter("userPasswordEncryptionFilter", new UserPasswordEncryptionFilter());
		registration.addMappingForUrlPatterns(null, false, "/login");
	}

}
