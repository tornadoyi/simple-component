/**
 * 
 */
package com.simple.dao.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
	 public String value();
}
