# Java项目的集成

本项目在之前web-building基础上进行完全重构，主要目标是拆分模块，简化配置，易于拓展、优化测试等几个方面，简化之前的复杂和冗余，使项目更加模块化，清晰度更高，可读性、可维护性、可扩展性等软件质量得到大幅提升。

## 说明

本项目集成了企业级项目开发中所涉及的大多数内容，如“关系管理”、“内容管理”、“文件管理”、“索引搜索”、“流程管理”、“通信安全”等等，涉及的Java组件众多，但整个项目规范统一、配置简洁、测试充分、架构合理，可简单移植到真实开发环境中。

项目基于前后端分离，作为后端，主要提供API接口，为了验证接口，项目中含有基于AngularJS1.×的前端代码，也可在此API之上新建一个基于Angular或Vue的独立的前端项目。

项目分为三个模块，common作为工具包，可被其他项目引用；core模块作为集成项目的核心包搭建了整个integration环境，包括统一的数据源和管理接口，基础配置，以及严格的单元测试等，所以在integration环境中的其他模块（如web）只需在Maven中引入core模块即统一了一致的开发环境，具体使用可见web模块。

## 数据

项目运行的基础数据是在配置中自动化生成的，配置好数据源并启动项目，即可直接运行，若需定制数据（如用户、初始化密码）可在配置文件中修改：
```
com.github.emailtohl.integration.core.config.CorePresetData
```
以及
```
com.github.emailtohl.integration.web.config.WebPresetData
```

## 项目启动

首先，web容器（tomcat）会引导web模块中的：
~~~
com.github.emailtohl.integration.web.ContainerBootstrap
~~~
该类创建了Spring上下文，并注册了Servlet、Listener、Filter。

初始化Spring时，同时设置了Spring运行的环境变量：
1. Profiles.DB_JNDI：从JNDI中查找数据源，当然也可以改为Profiles.DB_CONFIG，这样就是直接读取src/main/resources/config.properties中的数据源，逻辑位于：
```
com.github.emailtohl.integration.core.config.DataSourceConfiguration
```
2. Profiles.ENV_SERVLET：这主要区分单元测试环境和在实际Servlet容器环境下代码的逻辑。


## 配置

当maven下载完所有依赖包后，初始化项目:

首先创建数据库，项目默认通过JNDI获取数据源，这是为了避免数据泄漏的标准做法。所以需要在tomcat的context.xml中配置：
```xml
    <Resource name="jdbc/integration" type="javax.sql.DataSource"
		maxActive="20" maxIdle="5" maxWait="10000" username="postgres"
		password="123456" driverClassName="org.postgresql.Driver"
		defaultTransactionIsolation="READ_COMMITTED"
		url="jdbc:postgresql://localhost:5432/integration"></Resource>
```

若将web模块的ContainerBootstrap类中setActiveProfiles改为Profiles.DB_CONFIG，则core模块中的DataSourceConfiguration就会读取integration-core中src/main/resources/database.properties中的数据库配置：

```java
	rootContext.getEnvironment().setActiveProfiles(Profiles.DB_JNDI, Profiles.ENV_SERVLET);// 激活spring配置中的profile
```

项目会使用文件系统，配置位于integration-core的src/main/resources/config.properties中，若不配置，则默认使用与项目同级目录下的integration-data，该目录包括索引目录(integration-data/index)、资源目录(integration-data/resources)等，为了在浏览器上访问资源文件，还需在tomcat的server.xml中的Host标签下配置虚拟目录：

```xml
<Context docBase="/home/helei/programs/apache-tomcat-8.5.24/wtpwebapps/integration-data/resources" path="/web/resources" reloadable="true"/>
```

> 注意：第一次启动项目时，若没有integration-data/resources目录，则tomcat会报无法找到资源的异常：The main resource set specified [integration-data/resources] is not valid。不过启动一次后，该目录就会被创建，第二次就能正确启动。

登录时使用内置的账号：

> 1000/admin

若要自定义账号等数据，可在com.github.emailtohl.integration.core.config.PresetData中进行配置，并在该类同级的InitData中进行初始化。

## 注意

在引入core的模块中，若有修改用户信息的功能，则需要将web模块中的：
~~~
com.github.emailtohl.integration.web.aop.UserServiceProxy
~~~
和
~~~
com.github.emailtohl.integration.web.aop.RoleServiceProxy
~~~
拷贝一份，并在Spring中启动切面，这是用于同步更新Activiti的用户信息。

当然最好将修改用户信息的功能集中在web模块中进行管理。
