package com.github.emailtohl.integration.core.config;

import static com.github.emailtohl.integration.core.Profiles.DB_CONFIG;
import static com.github.emailtohl.integration.core.Profiles.DB_JNDI;
import static com.github.emailtohl.integration.core.Profiles.DB_RAM_H2;
import static com.github.emailtohl.integration.core.Profiles.ENV_NO_SERVLET;
import static com.github.emailtohl.integration.core.Profiles.ENV_SERVLET;

import java.io.File;
import java.util.Arrays;

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
 * 数据层统一配置
 * @author HeLei
 */
@Configuration
// 注意：main和test目录下各有一个resource目录，若有同样文件名的配置，maven会在测试环境下使用test目录下的配置
@PropertySource({ "classpath:database.properties", "classpath:config.properties" })
public class DataSourceConfiguration {
	private static final Logger LOG = LogManager.getLogger();
	
	/**
	 * 静态配置方法，该方法将在最早执行，这样才能读取properties配置
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
	@Profile(DB_RAM_H2)
	@Bean
	public DataSource embeddedDataSource() {
		return new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				// .addScripts("classpath:test-data.sql")
				.build();
	}

	@Profile(DB_CONFIG)
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

	@Profile({ DB_JNDI })
	@Bean(name = "jndi_dataSource")
	public DataSource jndiDataSource() {
		JndiDataSourceLookup lookup = new JndiDataSourceLookup();
		return lookup.getDataSource("jdbc/integration");
	}

	/**
	 * 在servlet容器中
	 * @param servletContext
	 * @return
	 */
	@Profile({ ENV_SERVLET })
	@Bean(name = "root")
	public File webRoot(ServletContext servletContext) {
		File f = new File(servletContext.getRealPath(""));
		LOG.info("web容器中的项目根目录是：{}", f.getAbsolutePath());
		return f;
	}
	
	/**
	 * 未在servlet容器中
	 * @return
	 */
	@Profile({ ENV_NO_SERVLET })
	@Bean(name = "root")
	public File root() {
		File f = new File("target");
		if (!f.exists()) {
			f.mkdirs();
		}
		LOG.debug("项目根目录是：{}", f.getAbsolutePath());
		return f;
	}
	
	/**
	 * 存放数据的目录，其下，有公开的resources目录，也有未公开的索引（index）等目录
	 * @return
	 */
	@Bean(name = "data")
	public File dataPath(@Named("root") File root) {
		String path = env.getProperty("dataPath");
		File dataPath;
		if (StringUtils.hasText(path)) {// 若有配置，则以配置为准
			dataPath = new File(path);
		} else {// 如果没有配置，则相对于项目目录进行创建
			if (contains(ENV_SERVLET)) {
				// 若在容器中，则在上级目录下创建数据目录
				dataPath = new File(root.getParentFile(), "integration-data");
			} else {
				// 若不在容器中，则在当前目录下创建数据目录
				dataPath = new File(root, "integration-data");
			}
		}
		if (!dataPath.exists())
			dataPath.mkdir();
		return dataPath;
	}
	
	/**
	 * web资源目录，在dataPath 存放数据的目录下，此目录为公开外部访问的根目录，在tomcat上需配置虚拟目录的地址与此目录相对应，如：
	 * <Context docBase="/home/helei/programs/apache-tomcat-8.5.24/wtpwebapps/integration-data/resources" path="/web/resources" reloadable="true"/>
	 * @param
	 * @return
	 */
	@Bean(name = "resources")
	public File resourcePath(@Named("data") File dataPath) {
		File resourcePath = new File(dataPath, "resources");
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

	/**
	 * 测试当前环境是否含有此名字
	 * @param envName
	 * @return
	 */
	public boolean contains(String envName) {
		return Arrays.stream(env.getActiveProfiles()).anyMatch(s -> envName.equals(s));
	}
	
}
