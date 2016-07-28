/**
 * 
 */
package com.simple.base.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */

public class ClassHelper {
    public static Class<?> getParameterizedType(Type type) {
        Class<?> ret = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type typeArg : parameterizedType.getActualTypeArguments()) {
                if (Class.class.isAssignableFrom(typeArg.getClass())) {
                    ret = (Class<?>) typeArg;
                } else {
                    ret = (Class<?>) ((ParameterizedType) typeArg).getRawType();
                }

            }
        }
        return ret;
    }
}
