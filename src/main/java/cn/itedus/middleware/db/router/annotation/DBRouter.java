package cn.itedus.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 10:24
 * @description: 负责设置需要被路由处理的DAO方法，作用到接口层就可以使用，路由注解的目的就是为了切面提供切点，同时获取方法中入参属性的某个字段，这个字段会作为路由字段存在
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DBRouter {
    String key() default "";

}
