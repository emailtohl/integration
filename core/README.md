# 核心模块

本模块是基于Spring的通用层，包括Spring环境下的基本配置、底层数据库连接、文件系统访问、消息、用户管理逻辑等。它统一了各类配置，可以被其他上层模块或项目（如web层、服务层）引用。

使用方法：
1. 在调用层的Spring配置导入本模块的CoreConfiguration；
2. 调用层Spring容器中设置Profiles:context.getEnvironment().setActiveProfiles( Profiles.DB_JNDI, Profiles.ENV_SERVLET );
