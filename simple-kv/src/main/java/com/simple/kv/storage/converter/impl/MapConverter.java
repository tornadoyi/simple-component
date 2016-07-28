package com.simple.kv.storage.converter.impl;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.reflect.BeanResolver;
import com.simple.kv.reflect.PropertyInfo;
import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.converter.IConverter;

public class MapConverter implements IConverter<Map<?, ?>> {

	/**
	 * <pre>
	 * 额外参数：
	 * 0 - Map的具体实现(Class)
	 * 1 - Map中key的类型信息(TypeInfo或Class)
	 * 2 - Map中value的类型信息(TypeInfo或Class)
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<?, ?> convert(Object value, Object... params) {
		if (value == null) {
			return null;
		}
		Class<?> mapCls = null;
		TypeInfo keyType = null, valueType = null;
		if (params != null && params.length > 0) {
			if (params[0] instanceof Class && params[0] != null) {
				mapCls = (Class<?>) params[0];
			}
			if (params.length > 1 && params[1] != null) {
				if (params[1] instanceof TypeInfo) {
					keyType = (TypeInfo) params[1];
				} else if (params[1] instanceof Class) {
					keyType = TypeInfo.getInstance((Class<?>) params[1]);
				}
			}
			if (params.length > 2 && params[2] != null) {
				if (params[2] instanceof TypeInfo) {
					valueType = (TypeInfo) params[2];
				} else if (params[2] instanceof Class) {
					valueType = TypeInfo.getInstance((Class<?>) params[2]);
				}
			}
		}
		Map<Object, Object> newMap = null;
		if (mapCls != null) {
			int mod = mapCls.getModifiers();
			if (!Modifier.isInterface(mod) && !Modifier.isAbstract(mod)) {
				try {
					newMap = (Map<Object, Object>) mapCls.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (newMap == null) {
				if (ConcurrentMap.class.isAssignableFrom(mapCls)) {
					newMap = CollectionUtil.newConcurrentMap();
				} else if (ConcurrentNavigableMap.class.isAssignableFrom(mapCls)) {
					newMap = new ConcurrentSkipListMap<Object, Object>();
				} else if (SortedMap.class.isAssignableFrom(mapCls)) {
					newMap = CollectionUtil.newTreeMap();
				}
			}
		}
		if (newMap == null) {
			newMap = CollectionUtil.newHashMap();
		}
		IConverter<?> keyConverter = null, valueConverter = null;
		if (keyType != null) {
			keyConverter = ConverterType.getConverter(keyType);
		}
		if (valueType != null) {
			valueConverter = ConverterType.getConverter(valueType);
		}
		if (value instanceof Map) {
			Map<?, ?> mapObj = (Map<?, ?>) value;
			for (Map.Entry<?, ?> entry : mapObj.entrySet()) {
				Object keyItem = entry.getKey(), valueItem = entry.getValue();
				if(keyItem != null && valueItem != null) {
					newMap.put(keyConverter != null ? keyConverter.convert(keyItem) : keyItem, valueConverter != null ? valueConverter.convert(valueItem) : valueItem);
				}
			}
		} else { // bean to map
			BeanResolver resolver = BeanResolver.getInstance(value.getClass());
			for(PropertyInfo pi : resolver.getPropertyInfos()) {
				Object valueItem = pi.readValue(value);
				if(valueItem != null) {
					String keyItem = pi.getName();
					newMap.put(keyConverter != null ? keyConverter.convert(keyItem) : keyItem, valueConverter != null ? valueConverter.convert(valueItem) : valueItem);
				}
			}
		}
		return newMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(Map<?, ?> value, Object... params) {
		if (value == null) {
			return "null";
		}
		Map<?, ?> map = (Map<?, ?>) value;
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean needSplit = false;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (needSplit) {
				sb.append(", ");
			} else {
				needSplit = true;
			}
			Object keyItem = entry.getKey(), valueItem = entry.getValue();
			sb.append(keyItem != null ? ((IConverter<Object>)ConverterType.getConverter(keyItem.getClass())).toString(keyItem) : "null").append(":");
			sb.append(valueItem != null ? ((IConverter<Object>)ConverterType.getConverter(valueItem.getClass())).toString(valueItem) : "null");
		}
		sb.append("}");
		return sb.toString();
	}

}
