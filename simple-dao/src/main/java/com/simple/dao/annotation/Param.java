package com.simple.dao.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.simple.dao.DaoParamType;


/**
 * DAO方法参数的注解，用于标识当前参数在SQL中的引用名，已经参数的类型。
 *
 
 * @version 1.0 2009-10-12 11:52:49
 * @since 1.0
 */
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Param {

    public String code() default "";

    public String alias() default "";

    public DaoParamType type() default DaoParamType.PARAM;

}
