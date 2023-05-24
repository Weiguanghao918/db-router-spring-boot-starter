package cn.itedus.middleware.db.router.dynamic;

import cn.itedus.middleware.db.router.DBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 10:24
 * @description: 动态数据源类
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DBContextHolder.getDBKey();
    }

}
