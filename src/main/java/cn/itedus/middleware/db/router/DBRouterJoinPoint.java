package cn.itedus.middleware.db.router;

import cn.itedus.middleware.db.router.annotation.DBRouter;
import cn.itedus.middleware.db.router.strategy.IDBRouterStrategy;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.Pipe;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 10:25
 * @description: 整个数据库路由计算的核心类，数据路由切面，通过自定义注解的方式，拦截被切面的方法，进行数据库路由
 */
@Aspect
public class DBRouterJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);

    private DBRouterConfig dbRouterConfig;

    private IDBRouterStrategy dbRouterStrategy;

    public DBRouterJoinPoint(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }

    @Pointcut("@annotation(cn.itedus.middleware.db.router.annotation.DBRouter)")
    public void aopPoint() {
    }

    /**
     * 所有需要分库分表的擦做，都需要使用自定义注解进行拦截，拦截后读取方法中的入参字段，再根据字段进行路由操作
     * 1. dbRouter.key() 确定根据哪个字段进行路由
     * 2. getAttrValue 根据数据库路由字段，从入参中读取出对应的值。比如路由 key 是 uId，那么就从入参对象 Obj 中获取到 uId 的值。
     * 3. dbRouterStrategy.doRouter(dbKeyAttr) 路由策略根据具体的路由值进行处理
     * 4. 路由处理完成比，就是放行。 jp.proceed();
     * 5. 最后 dbRouterStrategy 需要执行 clear 因为这里用到了 ThreadLocal 需要手动清空。
     *
     * @param jp       环绕通知中用于访问被通知方法的连节点信息的参数，可以获取到被通知方法的相关信息，如：目标对象、方法签名、方法参数等。
     * @param dbRouter 被通知方法的注解
     * @return 返回方法执行结果
     * @throws Throwable 异常处理
     */
    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable {
        String dbKey = dbRouter.key();
        if (StringUtils.isBlank(dbKey) && StringUtils.isBlank(dbRouterConfig.getRouterKey())) {
            throw new RuntimeException("annotation DBRouter key is null！");
        }
        dbKey = StringUtils.isNotBlank(dbKey) ? dbKey : dbRouterConfig.getRouterKey();
        // 路由属性
        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
        // 路由策略
        dbRouterStrategy.doRouter(dbKeyAttr);
        // 返回结果
        try {
            return jp.proceed();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    /**
     * 根据字段名获取参数对象指定的字段数据
     *
     * @param attr 字段名
     * @param args 参数列表
     * @return 对象指定字段的数据
     */
    public String getAttrValue(String attr, Object[] args) {
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {
                return arg.toString();
            }
        }

        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                // filedValue = BeanUtils.getProperty(arg, attr);
                // fix: 使用lombok时，uId这种字段的get方法与idea生成的get方法不同，会导致获取不到属性值，改成反射获取解决
                filedValue = String.valueOf(this.getValueByName(arg, attr));
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }

    /**
     * 获取对象的特定属性值
     *
     * @param item 对象
     * @param name 属性名
     * @return 属性值
     */
    private Object getValueByName(Object item, String name) {
        try {
            Field field = getFieldByName(item, name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(item);
            field.setAccessible(false);
            return o;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 根据名称获取方法，该方法同时兼顾继承类获取父类的属性
     *
     * @param item 对象
     * @param name 属性名
     * @return 该属性对应方法
     */
    private Field getFieldByName(Object item, String name) {
        try {
            Field field;
            try {
                field = item.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                field = item.getClass().getSuperclass().getDeclaredField(name);
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

}
