package com.simple.kv.storage.serialization.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将这个注解加在目标对象需要被执行的方法上，这些方法会在反序列化结束之后被执行。需要注意的是，这些方法包含公共、保护、默认（包）访问和私有方法，但不包括继承的方法。
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterDeserialization {

	/**
	 * 如果目标对象里有多个方法，将按这个字段的从小到大顺序依次执行，如果字段值一样，则不保证执行顺序
	 * 
	 * @return 方法执行的先后顺序
	 */
	public int order() default 0;

}
