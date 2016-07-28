package com.simple.kv.storage.converter.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.reflect.BeanResolver;
import com.simple.kv.reflect.PropertyInfo;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.converter.IConverter;

public class AmfObjectConverter implements IConverter<Object> {

	private static final Logger logger = Logger.getLogger(AmfObjectConverter.class);

	@Override
	public Object convert(Object value, Object... params) {
		if (value == null) {
			return null;
		} else if (value instanceof Long) {
			return value.toString();
		} else if (canDirectWrite(value)) {
			return value;
		} else if (value instanceof Collection<?>) {
			Collection<?> oc = (Collection<?>) value;
			Object[] array = new Object[oc.size()];
			Iterator<?> ite = oc.iterator();
			int index = 0;
			while (ite.hasNext()) {
				array[index++] = convert(ite.next());
			}
			return array;
		} else if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			Object array = Array.newInstance(value.getClass().getComponentType(), length);
			for (int i = 0; i < length; i++) {
				Array.set(array, i, convert(Array.get(value, i)));
			}
			return array;
		} else if (value instanceof Map) {
			Map<Object, Object> resultMap = CollectionUtil.newHashMap();
			@SuppressWarnings("unchecked")
			Map<Object, Object> object2Map = (Map<Object, Object>) value;
			for (Map.Entry<Object, Object> entry : object2Map.entrySet()) {
				Object k = entry.getKey();
				Object v = entry.getValue();
				resultMap.put(k.toString(), convert(v)); // only allow String key
			}
			return resultMap;
		} else {
			Map<Object, Object> resultMap = CollectionUtil.newHashMap();
			Class<?> cls = value.getClass();
			BeanResolver resolver = BeanResolver.getInstance(cls);
			for (PropertyInfo pi : resolver.getPropertyInfos()) {
				Method readMethod = pi.getReadMethod();
				String name = pi.getName();
				if (readMethod != null) {
					Object v = null;
					try {
						v = readMethod.invoke(value);
					} catch (Exception e) {
						logger.error("invoke read method error " + cls.getName() + " " + name, e);
					}
					if (v != null) {
						resultMap.put(name, convert(v));
					}
				}
			}
			return resultMap;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString(Object value, Object... params) {
		if (value == null) {
			return "null";
		}
		return ((IConverter<Object>) ConverterType.getConverter(value.getClass())).toString(value);
	}

	private static boolean canDirectWrite(Object o) {
		if (o instanceof String || o instanceof Character || o instanceof Number || o instanceof Boolean || o instanceof Date) {
			return true;
		}
		return false;
	}

}
