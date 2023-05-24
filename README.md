# DB-Router数据库路由组件

**系统架构**：基于 AOP、Spring 动态数据源切换、MyBatis 插件开发、散列算法等技术，实现的 SpringBoot Starter 数据库路由组件

**核心技术**：AOP、AbstractRoutingDataSource、MyBatis Plugin StatementHandler、扰动函数、哈希散列、ThreadLocal

**项目描述**：此组件项目是为了解决在分库分表场景下，开发一款可以应对自身业务场景多变特性，即支持个性的分库分表、只分库或者只分表以及双字段控制分库和分表，也可以自定义扩展监控、扫描、策略等规则，同时又能满足简单维护迭代的数据库路由组件。

**组件运行流程**：
![img.png](dbRouter.png)


