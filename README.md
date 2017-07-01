# Java项目的集成

**Java JavaScript 业务框架 spring springmvc springsecurity springdata JPA Hibernate search Envers Lucene angularjs1.× AdminLTE**

说明
----

之前的web-building，由于想包罗万象，同样功能有多份实现，所以异常复杂，这次对项目的重构，主要目标是拆分模块，简化配置，优化测试等几个方面，旨在简化之前的复杂和冗余，使项目更加模块化，清晰度更高，也更加易于学习和使用。

我是后端开发者，对于前端的开发仅限于程序部分，所以页面样式非常粗糙，但功能皆满足企业与工程化需求。项目中最有价值部分是common、user模块，这是一般应用程序中都需要的功能，message模块中演示了websocket的集群技术，具有学习价值。

在common模块中封装了的JPA，业务程序只需简单的继承他们即可实现动态查询、全文搜索、变更审计等功能，而lucene、ZtreeNode工具则用于文件系统的管理和搜索场景中（cms模块中有应用示例）。

本次重构，每个模块都各自独立，且架构统一，满足软件工程的一致性、可读性、可维护性等质量要求。单元测试方面，默认（spring profile配置）使用内存型数据库，此特性可在没有数据库的环境下完成单元测试，不仅保证了单元测试的质量，且使得maven的构建速度得到很大提高。

当maven下载完所有依赖包后，初始化项目:

首先创建数据库并配置web模块中的src/main/resources/database.properties

然后进入web模块中的src/test/java/com.github.emailtohl.integration.web.webTestConfig.InitDataSource类，执行main函数创建表和初始数据

项目默认通过JNDI获取数据源，这是为了避免数据泄漏的标准做法，所以需要在tomcat的context.xml中配置：
```xml
    <Resource name="jdbc/integration" type="javax.sql.DataSource"
		maxActive="20" maxIdle="5" maxWait="10000" username="postgres"
		password="123456" driverClassName="org.postgresql.Driver"
		defaultTransactionIsolation="READ_COMMITTED"
		url="jdbc:postgresql://localhost:5432/integration"></Resource>
```

当然也可以使用src/main/resources/database.properties中的数据库配置，在web模块src/main/java/com.github.emailtohl.integration.web.boot.ContainerBootstrap类中：

```java
		serviceContext.getEnvironment().setActiveProfiles(
			DataSourceConfiguration.DB_JNDI,
			DataSourceConfiguration.ENV_SERVLET
		);// 激活spring配置中的profile
```

将
```java
DataSourceConfiguration.DB_JNDI
```
替换为
```java
DataSourceConfiguration.DB_CONFIG
```
即可。

项目成功启动一次后，会将cms功能需要的模板文件复制到可以读取的文件空间中，若使用cms的功能，还需在tomcat的server.xml中配置虚拟目录：

```xml
<Context debug="0" docBase="H:\server\apache-tomcat-9.0.0.M17\wtpwebapps\integration-data\resources" path="/web/resources" reloadable="true"/>
```

登录时使用内置的账号：

> administrator@administrator/123456

若要自定义账号等数据，可在初始化数据库之前，进入src/test/java/com.github.emailtohl.integration.web.WebTestData中进行配置