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
        //在获取哈希值之前先对hashCode的高位和低位进行异或操作，然后在进行一次位运算，这样做可以使得哈希值更加随机，减少哈希碰撞的概率。。
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));


        /**
         * 库表索引；相当于是把一个长条的桶，切割成段，对应分库分表中的库编号和表编号
         * 在这里对这个公式的意思做一个简要说明：
         * 1. 上一步的idx只是根据HashMap算法模型，使用散列+扰动函数计算出的索引位置，这个时候的索引位置就类似于HashMap的索引位置
         * 2. 库表总量按照2的幂次方设置，比如目前2个库*4个表= 8个表
         * 3. 将计算出的索引位置idx分摊到8个表中，这个时候只需要按照HashMap的数据结构分散到库表中即可
         * 4 比如：idx=3，那么它就是在1库3表，idx=7那么他就是在2库的3表，因为1库4表+2库3表正好是7，那么自然就有了下面的这个分配算法了。
         * */
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        // 设置到 ThreadLocal；关于 ThreadLocal 的使用场景和源码介绍；https://bugstack.cn/md/java/interview/2020-09-23-%E9%9D%A2%E7%BB%8F%E6%89%8B%E5%86%8C%20%C2%B7%20%E7%AC%AC12%E7%AF%87%E3%80%8A%E9%9D%A2%E8%AF%95%E5%AE%98%EF%BC%8CThreadLocal%20%E4%BD%A0%E8%A6%81%E8%BF%99%E4%B9%88%E9%97%AE%EF%BC%8C%E6%88%91%E5%B0%B1%E6%8C%82%E4%BA%86%EF%BC%81%E3%80%8B.html
        //这里使用ThreadLocal的好处在于，每个线程独有一份，再线程上下文中都可以进行获取。
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
