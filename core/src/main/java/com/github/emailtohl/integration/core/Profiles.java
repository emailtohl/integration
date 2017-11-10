package com.github.emailtohl.integration.core;

/**
 * 本模块被Spring管理，引入本模块需要在Spring配置中设置Profiles:
 * Context.getEnvironment().setActiveProfiles( Profiles.DB_JNDI, Profiles.ENV_SERVLET );
 * 
 * @author HeLei
 */
public interface Profiles {
	// 以下三个DB项必选一个
	String DB_JNDI = "db_jndi";
	String DB_CONFIG = "db_config";
	String DB_RAM_H2 = "db_ram_h2";

	// 以下两个环境项必选一个
	// 在容器中的环境
	String ENV_SERVLET = "env_servlet";
	// 在容器中的环境
	String ENV_NO_SERVLET = "env_no_servlet";
}
