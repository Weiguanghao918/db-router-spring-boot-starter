package cn.itedus.middleware.db.router;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 10:25
 * @description: 数据源基础配置
 */
public class DBRouterBase {

    private String tbIdx;

    public String getTbIdx() {
        return DBContextHolder.getTBKey();
    }

}
