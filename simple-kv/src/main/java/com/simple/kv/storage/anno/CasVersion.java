package com.simple.kv.storage.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 执行cas操作时，加在作为cas version的参数上
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CasVersion {

}
