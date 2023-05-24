package cn.itedus.middleware.db.router.annotation;

import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.annotation.*;

/**
 * @author: Guanghao Wei
 * @date: 2023-05-24 13:40
 * @description: 路由策略，分表标记
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    boolean splitTable() default false;
}
