package com.simple.kv.storage.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.simple.kv.storage.OperationType;

/**
 * 标记在方法上的注解，表明这是一个需要生成实现的持久化相关方法
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Storage {

	/**
	 * 存储配置的key，根据这个key可以从注册的配置中找到具体的存储配置
	 */
	public String storageConfigKey();
	
	/**
	 * 操作类型
	 */
	public OperationType type();

}
