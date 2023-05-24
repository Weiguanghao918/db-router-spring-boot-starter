package cn.itedus.middleware.db.router;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 10:25
 * @description: 本地线程设置路由结果，使用了两个本地线程类记录分库、分表的路由结果
 */
public class DBContextHolder {
    //使用ThreadLocal来保存上下文的变量信息，保证存储进去的信息只能被当前的线程读取到，并且线程之间不会受到影响，主要有两个作用：
    //1. 保存每个线程独享的对象，未每个线程都创建一个副本，这样每个线程都可以修改自己所拥有的副本，而不会影响其他线程的副本，确保了线程安全
    //2. 每个线程内需要独立保存信息，以便供其他方法更方便地获取该信息的场景
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
