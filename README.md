# Java项目的集成

**Java JavaScript 业务框架 spring springmvc springsecurity springdata JPA Hibernate search Envers Lucene angularjs1.× AdminLTE**

说明
----

之前的web-building，由于想包罗万象，同样功能有多份实现，所以异常复杂，这次对项目的重构，主要目标是拆分模块，简化配置，优化测试等几个方面，旨在简化之前的复杂和冗余，使项目更加模块化，清晰度更高，也更加易于学习和使用。
我是后端开发者，前端的开发仅限于程序部分，所以页面样式非常粗糙。程序部分最有价值的是common、user模块，这是普通项目中都需要的功能。
其中，common模块主要封装了JPA，让动态查询，全文搜索更方便，其他如lucene、ZtreeNode工具则很好地对文件系统进行管理和搜索（cms模块中有应用示例）。

本次重构，每个模块都各自独立，且架构统一，最大化地保证项目工程的一致性、可读性、可维护性，maven的单元测试默认（spring profile配置）使用内存型数据库，不再依赖外部环境的特性使得单元测试质量更高，且提高构建速度。

初始化项目时。首先创建数据库并配置src/main/resources/database.properties
然后进入web模块src/test/java/com.github.emailtohl.integration.web.webTestConfig.InitDataSource执行main函数创建表和初始数据

项目默认通过JNDI获取数据源，配置在web模块src/main/java/com.github.emailtohl.integration.web.boot.ContainerBootstrap中：
```java
		serviceContext.getEnvironment().setActiveProfiles(
			DataSourceConfiguration.DB_JNDI,
			DataSourceConfiguration.ENV_SERVLET
		);// 激活spring配置中的profile
```
DB_JNDI说明使用JNDI获取数据源，这就需要在tomcat的context.xml中配置：
```xml
    <Resource name="jdbc/integration" type="javax.sql.DataSource"
		maxActive="20" maxIdle="5" maxWait="10000" username="postgres"
		password="123456" driverClassName="org.postgresql.Driver"
		defaultTransactionIsolation="READ_COMMITTED"
		url="jdbc:postgresql://localhost:5432/integration"></Resource>
```

若将DB_JNDI切换为DB_CONFIG，则系统会使用src/main/resources/database.properties中的数据库配置

项目成功启动一次后，若使用cms的功能，还需在tomcat的server.xml中配置虚拟目录：
```xml
<Context debug="0" docBase="H:\server\apache-tomcat-9.0.0.M17\wtpwebapps\integration-data\resources" path="/web/resources" reloadable="true"/>
```

登录使用账号：

> administrator@administrator/123456

也可以在初始化数据库之前，进入src/test/java/com.github.emailtohl.integration.web.WebTestData中进行配置