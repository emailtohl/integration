# Java项目的集成

**Java JavaScript 业务框架 spring springmvc springsecurity springdata JPA Hibernate search Envers Lucene Activiti angularjs1.× AdminLTE**

说明
----

之前的web-building，由于想包罗万象，同样功能有多份实现，所以十分复杂，这次对项目的重构，主要目标是拆分模块，简化配置，优化测试等几个方面，旨在简化之前的复杂和冗余，使项目更加模块化，清晰度更高，也更加易于学习和使用。

这是第二次重构了，第一次重构后的版本已转存到milestone分支上，master主分支则在二次重构过程中，本次主要重点在后端架构上，目标是真实的业务系统，而不再是简单demo。前端暂时仍使用Angular1.×补齐后台功能，后续计划用Angular或Vue逐步替代。

本次重构，项目分为三个模块，其中common仍然保留原状，可被其他项目引用；本次主要开发是core模块，它架设了整个integration环境，包括提供基于Spring的基础配置，统一访问接口，严格单元测试等，这让配置和接口统一化，如此一来在integration环境中的所有上层模块（如web）只需引入core即拥有统一开发环境。具体使用可见web模块。

另外core模块依托于JPA，内置了基础业务数据，启动项目即可及时观察效果。

当maven下载完所有依赖包后，初始化项目:

首先创建数据库，项目默认通过JNDI获取数据源，这是为了避免数据泄漏的标准做法，所以需要在tomcat的context.xml中配置：
```xml
    <Resource name="jdbc/integration" type="javax.sql.DataSource"
		maxActive="20" maxIdle="5" maxWait="10000" username="postgres"
		password="123456" driverClassName="org.postgresql.Driver"
		defaultTransactionIsolation="READ_COMMITTED"
		url="jdbc:postgresql://localhost:5432/integration"></Resource>
```

当然也可以使用src/main/resources/database.properties中的数据库配置，将web模块的ContainerBootstrap类中setActiveProfiles为Profiles.DB_CONFIG，这样core模块中的DataSourceConfiguration就会用配置来创建数据源。：

```java
	rootContext.getEnvironment().setActiveProfiles(Profiles.DB_JNDI, Profiles.ENV_SERVLET);// 激活spring配置中的profile
```

项目会使用文件空间中，还需在tomcat的server.xml中的Host标签下配置虚拟目录：

```xml
<Context docBase="/home/helei/programs/apache-tomcat-8.5.24/wtpwebapps/integration-data/resources" path="/web/resources" reloadable="true"/>
```

登录时使用内置的账号：

> 1000/admin

若要自定义账号等数据，可在com.github.emailtohl.integration.core.config.PresetData中进行配置，并在该类同级的InitData中进行初始化。