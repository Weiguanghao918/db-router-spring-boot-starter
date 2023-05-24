package cn.itedus.middleware.db.router;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 10:25
 * @description: 本地线程设置路由结果，使用了两个本地线程类记录分库、分表的路由结果
 */
public class DBContextHolder {

    private static final ThreadLocal<String> dbKey = new ThreadLocal<String>();
    private static final ThreadLocal<String> tbKey = new ThreadLocal<String>();

    public static void setDBKey(String dbKeyIdx){
        dbKey.set(dbKeyIdx);
    }

    public static String getDBKey(){
        return dbKey.get();
    }

    public static void setTBKey(String tbKeyIdx){
        tbKey.set(tbKeyIdx);
    }

    public static String getTBKey(){
        return tbKey.get();
    }

    public static void clearDBKey(){
        dbKey.remove();
    }

    public static void clearTBKey(){
        tbKey.remove();
    }

}
