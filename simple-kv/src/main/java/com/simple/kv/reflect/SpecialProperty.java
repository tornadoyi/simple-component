package com.simple.kv.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.simple.base.util.DateTimeUtil;

/**
 * 表明对bean中某个属性的特殊处理，可以放在getter、setter方法的其中一项上，如果这两个方法都存在该注解，则按优先选取getter方法
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecialProperty {

	/**
	 * 如果为true，则表明bean在toString的时候，忽略该属性，默认为false
	 * @return
	 */
	public boolean notToString() default false;
	
	/**
	 * 日期时间的格式，默认是yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public String dateTimeFormat() default DateTimeUtil.YMD_HMS;
	
	/**
	 * 如果为true，则在BeanResolver中忽略这个属性
	 * @return
	 */
	public boolean ignore() default false;
	
}
