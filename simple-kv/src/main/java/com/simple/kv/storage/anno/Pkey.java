package com.simple.kv.storage.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <li>该注解加在作为key的参数上，如果是多个字段作为联合key，请使用元组传参，具体参见元组工厂类{@link com.happyelements.rdcenter.commons.util.tuple.Tuple}</li> <li>批量get或批量delete时，
 * 加在用于传递多个key的集合上；批量set时，加在用于传递key及value的Map上(Map的key作为key)，或是由实现了KeyParamAware接口的元素组成的value集合上</li> <li>通常情况下该注解标记的参数同时也作为mysql分表计算的参数；如果是联合key，则以元组中第一个参数作为mysql分表计算的参数</li>
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pkey {

	/**
	 * key对应的名称，当使用db存储时，会将这个名称与表的列名相匹配，默认为"key"，如果是联合key，要写全每个字段的名称
	 * 
	 * @return
	 */
	public String[] value() default "key";
	

}
