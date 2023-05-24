package cn.itedus.middleware.db.router.dynamic;

import cn.itedus.middleware.db.router.DBContextHolder;
import cn.itedus.middleware.db.router.annotation.DBRouterStrategy;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 11:16
 * @description: Mybatis拦截器，通过对SQL语句的拦截处理，修改分表信息
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DynamicMybatisPlugin implements Interceptor {

    //(from|into|update): 这是一个分组，用于匹配三个关键字之一：from、into或update。这里使用了竖线 | 表示逻辑或的关系，表示可以匹配其中任意一个关键字。
    //[\\s]{1,}: 这是一个字符类，匹配空白字符（包括空格、制表符、换行符等）。\\s 表示空白字符，{1,} 表示匹配一个或多个连续的空白字符。
    //(\\w{1,}): 这是另一个分组，用于匹配一个或多个连续的单词字符。\\w 表示单词字符（包括字母、数字、下划线），{1,} 表示匹配一个或多个连续的单词字符。
    // 例如，它可以匹配以下字符串：
    // SELECT * FROM table
    // INSERT INTO table
    // UPDATE table SET column = value
    private Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //通过下面三行代码可以获取到目标SQL语句的相关信息，并对其进行进一步的处理或者添加自定义逻辑
        //获取StatementHandle对象，该对象是MyBatis中用于处理数据库操作的接口，负责处理SQL语句的执行和结果的处理
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        //MetaObject是MyBatis提供的一个用于简化反射操作的工具类
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        //用反射对象获取到MappedStatement对象，该对象映射了一个SQL语句，包含了SQL语句的相关信息，如命名空间、SQL语句、参数映射等。
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        //获取自定义注解判断是否进行分表操作
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        Class<?> clazz = Class.forName(className);
        DBRouterStrategy dbRouterStrategy = clazz.getAnnotation(DBRouterStrategy.class);
        if (null == dbRouterStrategy || !dbRouterStrategy.splitTable()) {
            return invocation.proceed();
        }

        //获取SQL
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        //替换SQL表名USER为USER_03
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if (matcher.find()) {
            tableName = matcher.group().trim();
        }
        assert null != tableName;
        String replaceSql = matcher.replaceAll(tableName + "_" + DBContextHolder.getTBKey());

        //通过反射修改SQL语句
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);

        return invocation.proceed();
    }
}
