package com.simple.kv.storage.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <li>当执行新增或修改操作时，该注解加在作为value的字段上</li> <li>批量get或批量delete时， 加在用于传递多个value的集合上；批量set时，加在用于传递key及value的Map上(Map的value作为value)，或是由实现了KeyParamAware接口的元素组成的value集合上 <li>value的存储使用HeSerialization机制</li>
 * <li>对于mysql，value所在的列名为"value"</li>
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PValue {

}
