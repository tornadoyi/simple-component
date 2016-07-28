package com.simple.kv.storage.converter.impl;

import java.util.Map;

import com.simple.kv.reflect.BeanResolver;
import com.simple.kv.reflect.PropertyInfo;
import com.simple.kv.reflect.SpecialProperty;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.converter.IConverter;

public class BeanConverter implements IConverter<Object> {

	@Override
	public Object convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		Object result = null;
		Class<?> beanClass = null;
		if(params != null && params.length > 0 && params[0] != null) {
			if(params[0] instanceof Class) {
				beanClass = (Class<?>)params[0];
			} else {
				result = params[0];
				beanClass = result.getClass();
			}
		}
		if(beanClass == null) {
			return value;
		}
		BeanResolver beanResolver = BeanResolver.getInstance(beanClass);
		if(result == null) {
			result = beanResolver.newInstance();
		}
		if(value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>)value;
			for(PropertyInfo pi : beanResolver.getPropertyInfos()) {
				if(pi.canWrite()) {
					Object valueItem = map.get(pi.getName());
					if(valueItem != null) {
						pi.writeValue(result, ConverterType.getConverter(pi.getType(), pi.getSpecialProperty()).convert(valueItem));
					}
				}
			}
		} else {
			BeanResolver valueResolver = BeanResolver.getInstance(value.getClass());
			for(PropertyInfo pSrc : valueResolver.getPropertyInfos()) {
				if(pSrc.canRead()) {
					PropertyInfo pDest = beanResolver.getPropertyInfo(pSrc.getName());
					if(pDest != null && pDest.canWrite()) {
						Object fieldValue = pSrc.readValue(value);
						if(fieldValue != null) {
							pDest.writeValue(result, ConverterType.getConverter(pDest.getType(), pDest.getSpecialProperty()).convert(fieldValue));
						}
					}
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(Object value, Object... params) {
		if(value == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean needSplit = false;
		BeanResolver beanResolver = BeanResolver.getInstance(value.getClass());
		for(PropertyInfo pi : beanResolver.getPropertyInfos()) {
			if(pi.canRead()) {
				SpecialProperty sp = pi.getSpecialProperty();
				if(sp == null || !sp.notToString()) {
					if(needSplit) {
						sb.append(", ");
					} else {
						needSplit = true;
					}
					Object fieldValue = pi.readValue(value);
					sb.append(pi.getName()).append(":").append(fieldValue != null ? ((IConverter<Object>)ConverterType.getConverter(pi.getType(), sp)).toString(fieldValue) : "null");
				}
			}
		}
		sb.append("}");
		return sb.toString();
	}

}
