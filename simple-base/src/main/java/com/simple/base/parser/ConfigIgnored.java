package com.simple.base.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 指定Bean的属性是否是从配置文件中解析。默认被配置了的属性将被忽略。
 *
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface ConfigIgnored {

    /**
     * 是否在解析配置信息时忽略此属性。默认是忽略。
     * @return 如果返回TRUE，则解析配置忽略此属性；否则将尝试从配置中为此属性赋值。
     */
    public boolean ignore() default true;
}
