package com.simple.kv.storage.converter.impl;

import java.lang.reflect.Array;
import java.util.Collection;

import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.converter.IConverter;


public class ArrayConverter implements IConverter<Object> {

	/**
	 * <pre>
	 * 额外参数：
	 * 0 - 数组类型(TypeInfo或Class)
	 * </pre>
	 */
	@Override
	public Object convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		if (value instanceof Collection) {
			value = ((Collection<?>) value).toArray();
		}
		if (value.getClass().isArray()) {
			TypeInfo arrayType = null;
			if (params != null && params.length > 0 && params[0] != null) {
				if (params[0] instanceof TypeInfo) {
					arrayType = (TypeInfo) params[0];
				} else if (params[0] instanceof Class) {
					arrayType = TypeInfo.getInstance((Class<?>) params[0]);
				}
			}
			if (arrayType == null) {
				arrayType = TypeInfo.getInstance(value.getClass().getComponentType());
			}
			int length = Array.getLength(value);
			Object target = Array.newInstance(arrayType.getRawClass(), length);
			IConverter<?> converter = ConverterType.getConverter(arrayType);
			for (int i = 0; i < length; i++) {
				Object item = Array.get(value, i);
				Array.set(target, i, converter.convert(item));
			}
			return target;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(Object value, Object... params) {
		if(value == null) {
			return "null";
		}
		if (value.getClass().isArray()) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			int len = Array.getLength(value);
			for (int index = 0; index < len; index++) {
				if (index != 0) {
					sb.append(", ");
				}
				Object item = Array.get(value, index);
				sb.append(item != null ? ((IConverter<Object>)ConverterType.getConverter(item.getClass())).toString(item) : "null");
			}
			sb.append("]");
			return sb.toString();
		} else {
			return "null";
		}
	}

}
