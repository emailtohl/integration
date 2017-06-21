package com.github.emailtohl.integration.web.config;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.util.StringUtils;

/**
 * 数据源配置
 * 
 * @author HeLei
 * @date 2017.06.12
 */
@Configuration
@PropertySource({ "classpath:database.properties", "classpath:config.properties" })
public class DataSourceConfiguration {
	private static final Logger logger = LogManager.getLogger();
	public static final String JNDI_POSTGRESQL_DB = "jndi_postgresql_db";
	public static final String POSTGRESQL_DB = "postgresql_db";
	public static final String H2_RAM_DB = "h2_ram_db";
	
	// 测试环境的目录
	public static final String ENV_TEST_PATH = "env_test_path";
	// servlet容器中的目录
	public static final String ENV_SERVLET_PATH = "env_servlet_path";

	/**
	 * 静态配置方法，该方法将在最早执行，这样才能读取properties配置
	 * 
	 * @return
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	/**
	 * 将@PropertySource中引入的属性封装到Environment
	 */
	@Inject
	Environment env;

	@Value("${jdbc.driverClassName}")
	String driverClassName;
	@Value("${jdbc.url}")
	String url;
	@Value("${jdbc.username}")
	String username;
	@Value("${jdbc.password}")
	String password;

	
	/**
	 * 内存数据库
	 * 
	 * @return
	 */
	@Profile(H2_RAM_DB)
	@Bean
	public DataSource embeddedDataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
				// .addScripts("classpath:test-data.sql")
				.build();
	}

	@Profile(POSTGRESQL_DB)
	@Bean(name = "pool_dataSource")
	public DataSource tomcatJdbc() {
		// 创建连接池属性对象
		PoolProperties poolProps = new PoolProperties();
		poolProps.setUrl(url);
		poolProps.setDriverClassName(driverClassName);
		poolProps.setUsername(username);
		poolProps.setPassword(password);
		// 创建连接池, 使用了 tomcat 提供的的实现，它实现了 javax.sql.DataSource 接口
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
		// 为连接池设置属性
		dataSource.setPoolProperties(poolProps);
		return dataSource;
	}

	@Profile({ JNDI_POSTGRESQL_DB })
	@Bean(name = "jndi_dataSource")
	public DataSource jndiDataSource() {
		JndiDataSourceLookup lookup = new JndiDataSourceLookup();
		return lookup.getDataSource("jdbc/integration");
	}
	

	/**
	 * 项目目录
	 * 未在容器中，则返回项目的所在目录
	 * @return integration-web\target\test-classes
	 */
	@Profile(ENV_TEST_PATH)
	@Bean(name = "root")
	public File projectRoot() {
//		File f = new File(getClass().getResource("/").getFile());
		File f = new File("target");
		if (!f.exists()) {
			f.mkdirs();
		}
		logger.debug("项目根目录是：{}", f.getAbsolutePath());
		return f;
	}
	
	/**
	 * 项目在目录
	 * 在web容器中的根目录
	 * @param servletContext 被注入进来的容器上下文
	 * @return
	 */
	@Profile(ENV_SERVLET_PATH)
	@Bean(name = "root")
	public File webRoot(ServletContext servletContext) {
		File f = new File(servletContext.getRealPath(""));
		logger.debug("web容器中的项目根目录是：{}", f.getAbsolutePath());
		return f;
	}
	
	/**
	 * 数据目录
	 * @return
	 */
	@Bean(name = "data")
	public File dataPath(@Named("root") File root) {
		String path = env.getProperty("dataPath");
		File dataPath;
		if (StringUtils.hasText(path)) {// 若有配置，则以配置为准
			dataPath = new File(path);
		} else {// 如果没有配置，则相对于项目目录进行创建
			if (contains(ENV_SERVLET_PATH)) {// 如果是在web容器中，为了避免发布时被清除，在上级目录中创建一个data目录
				dataPath = new File(root.getParentFile(), "data");
			} else {// 否则，应该是在测试环境下，那么就在本项目的target目录下创建一个data目录
				dataPath = new File(root, "data");
			}
		}
		if (!dataPath.exists())
			dataPath.mkdir();
		return dataPath;
	}
	
	/**
	 * web资源目录
	 * @param servletContext 被注入进来的容器上下文
	 * @return
	 */
	@Profile({ ENV_SERVLET_PATH })
	@Bean(name = "resources")
	public File resourcePath(@Named("root") File root) {
		File resourcePath = new File(root, "resources");
		if (!resourcePath.exists())
			resourcePath.mkdir();
		return resourcePath;
	}
	
	/**
	 * lucene存放索引的目录
	 * @param dataPath
	 * @return
	 */
	@Bean(name = "indexBase")
	public File indexBase(@Named("data") File dataPath) {
		File index = new File(dataPath, "index");
		if (!index.exists()) {
			index.mkdir();
		}
		return index;
	}

	@Bean(name = "templatesPath")
	public File templatesPath(@Named("root") File root) {
		String path = env.getProperty("templatesPath");
		File templatesPath;
		if (StringUtils.hasText(path)) {
			templatesPath = new File(path);
		} else {
			templatesPath = new File(root, "templates");
		}
		if (!templatesPath.exists())
			templatesPath.mkdir();
		return templatesPath;
	}
	
	/**
	 * 测试当前环境是否含有此名字
	 * @param envName
	 * @return
	 */
	public boolean contains(String envName) {
		for (String s : env.getActiveProfiles()) {
			if (envName.equals(s)) {
				return true;
			}
		}
		return false;
	}
}
