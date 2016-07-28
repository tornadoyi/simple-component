package com.simple.kv.storage.converter.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collection;

import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.converter.IConverter;

@SuppressWarnings("rawtypes")
public abstract class CollectionConverter<T extends Collection> implements IConverter<T> {

	/**
	 * 得到具体Collection的实现类
	 * @param cls 继承了Collection的接口或者实现了Collection的抽象类
	 * @return 初始化好的Collection空对象
	 */
	protected abstract T getCollectionImplement(Class<?> cls);
	
	/**
	 * <pre>
	 * 额外参数：
	 * 0 - 集合类的具体实现(Class)
	 * 1 - 集合中元素的类型信息(TypeInfo或Class)
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T convert(Object value, Object... params) {
		if(value == null) {
			return null;
		}
		if(value instanceof Collection) {
			value = ((Collection<?>) value).toArray();
		}
		if (value.getClass().isArray()) {
			Class<?> colCls = null;
			TypeInfo genericType = null;
			if(params != null && params.length > 0) {
				if(params[0] instanceof Class && params[0] != null) {
					colCls = (Class<?>)params[0];
				}
				if(params.length > 1 && params[1] != null) {
					if (params[1] instanceof TypeInfo) {
						genericType = (TypeInfo) params[1];
					} else if (params[1] instanceof Class) {
						genericType = TypeInfo.getInstance((Class<?>) params[1]);
					}
				}
			}
			T newCol = null;
			if(colCls != null) {
				int mod = colCls.getModifiers();
				if(!Modifier.isInterface(mod) && !Modifier.isAbstract(mod)) {
					try {
						newCol = (T)colCls.newInstance();
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}
			if(newCol == null) {
				newCol = getCollectionImplement(colCls);
			}
			int length = Array.getLength(value);
			IConverter<?> converter = null;
			if(genericType != null) {
				converter = ConverterType.getConverter(genericType);
			}
			for (int i = 0; i < length; i++) {
				Object item = Array.get(value, i);
				newCol.add(converter != null ? converter.convert(item) : item);
			}
			return newCol;
		}
		return null;
	}

	@Override
	public String toString(T value, Object... params) {
		if(value == null) {
			return "null";
		}
		return ConverterType.TYPE_ARRAY.toString(value.toArray());
	}
	
}
