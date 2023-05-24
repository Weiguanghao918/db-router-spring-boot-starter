package cn.itedus.middleware.db.router.strategy.impl;

import cn.itedus.middleware.db.router.DBContextHolder;
import cn.itedus.middleware.db.router.DBRouterConfig;
import cn.itedus.middleware.db.router.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 11:16
 * @description: 哈希路由
 */
public class DBRouterStrategyHashCode implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyHashCode.class);

    private DBRouterConfig dbRouterConfig;

    public DBRouterStrategyHashCode(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();

        // 扰动函数；在 JDK 的 HashMap 中，对于一个元素的存放，需要进行哈希散列。而为了让散列更加均匀，所以添加了扰动函数。扩展学习；https://mp.weixin.qq.com/s/CySTVqEDK9-K1MRUwBKRCg
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));

        // 库表索引；相当于是把一个长条的桶，切割成段，对应分库分表中的库编号和表编号
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        // 设置到 ThreadLocal；关于 ThreadLocal 的使用场景和源码介绍；https://bugstack.cn/md/java/interview/2020-09-23-%E9%9D%A2%E7%BB%8F%E6%89%8B%E5%86%8C%20%C2%B7%20%E7%AC%AC12%E7%AF%87%E3%80%8A%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%8CThreadLocal%20%E4%BD%A0%E8%A6%81%E8%BF%99%E4%B9%88%E9%97%AE%EF%BC%8C%E6%88%91%E5%B0%B1%E6%8C%82%E4%BA%86%EF%BC%81%E3%80%8B.html
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
        logger.debug("数据库路由 dbIdx：{} tbIdx：{}",  dbIdx, tbIdx);
    }

    @Override
    public void setDBKey(int dbIdx) {
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%03d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear(){
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }

}
