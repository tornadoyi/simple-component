package com.simple.kv.storage.serialization.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.simple.kv.storage.serialization.HeSerializableType;

/**
 * 这个注解加在需要被序列化的字段上，基类中加了这个注解的字段也会被序列化，需要保证基类与子类中该注解的key不重复
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HeSerializableField {

	/**
	 * 字段或参数序列化后的标记，为了保证这个标记尽量小，因此这个key的数值范围为0~127(不允许为负)。
	 * key在本类及其基类中不得重复。
	 * @return
	 */
	public byte key();
	
	/**
	 * 字段或参数序列化时的类型，主要用于标记一些需要特殊处理的类型，
	 * 比如tinyInt、smallInt、mediumInt、unsignedTinyInt、unsignedSmallInt、unsignedMediumInt、smallString等，
	 * 其他情况不用标记，将使用field或参数本身的类型，类型的集合由枚举HeSerializableType定义
	 * @return
	 */
	public HeSerializableType type() default HeSerializableType.DEFAULT;
	
	/**
	 * 字段或参数的具体实现类，比如对于java.util.List，可以指定它的具体实现类，否则默认为java.util.ArrayList
	 * @return
	 */
	public Class<?> clazz() default void.class;
	
}
