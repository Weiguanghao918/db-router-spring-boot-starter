package cn.itedus.middleware.db.router.dynamic;

import cn.itedus.middleware.db.router.DBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 10:24
 * @description: 动态数据源类
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * AbstractRoutingDataSource是SpringFramework提供的一个抽象类，多用于多数据源切换，主要作用就是根据路由的一个Key决定使用哪个数据源。
     * 核心方法是determineCurrentLookupKey()用来表示当前使用的数据源，当应用程序请求一个连接时，Spring会通过这个方法获取当前使用的数据源的key，
     * 再根据key从预先定义的多个数据源中选择一个具体的数据源，选择完数据源后，Spring就会将请求路由到这个数据源上，并将请求转发到这个数据源进行处理。
     * @return 数据源
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DBContextHolder.getDBKey();
    }

}
