package com.simple.kv.storage.converter;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.reflect.SpecialProperty;
import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.converter.impl.AmfObjectConverter;
import com.simple.kv.storage.converter.impl.ArrayConverter;
import com.simple.kv.storage.converter.impl.BeanConverter;
import com.simple.kv.storage.converter.impl.BooleanConverter;
import com.simple.kv.storage.converter.impl.ByteConverter;
import com.simple.kv.storage.converter.impl.CharacterConverter;
import com.simple.kv.storage.converter.impl.DateConverter;
import com.simple.kv.storage.converter.impl.DefaultConverter;
import com.simple.kv.storage.converter.impl.DoubleConverter;
import com.simple.kv.storage.converter.impl.FloatConverter;
import com.simple.kv.storage.converter.impl.IntegerConverter;
import com.simple.kv.storage.converter.impl.ListConverter;
import com.simple.kv.storage.converter.impl.LongConverter;
import com.simple.kv.storage.converter.impl.MapConverter;
import com.simple.kv.storage.converter.impl.SetConverter;
import com.simple.kv.storage.converter.impl.ShortConverter;
import com.simple.kv.storage.converter.impl.SpecialConverter;
import com.simple.kv.storage.converter.impl.StringConverter;

/**
 * 数据转换类型的枚举类
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public enum ConverterType {

	TYPE_BYTE(new ByteConverter(), byte.class, Byte.class),
	TYPE_SHORT(new ShortConverter(), short.class, Short.class),
	TYPE_INTEGER(new IntegerConverter(), int.class, Integer.class),
	TYPE_LONG(new LongConverter(), long.class, Long.class),
	TYPE_FLOAT(new FloatConverter(), float.class, Float.class),
	TYPE_DOUBLE(new DoubleConverter(), double.class, Double.class),
	TYPE_BOOLEAN(new BooleanConverter(), boolean.class, Boolean.class),
	TYPE_CHARACTER(new CharacterConverter(), char.class, Character.class),
	TYPE_STRING(new StringConverter(), String.class),
	TYPE_DATE(new DateConverter(), Date.class),
	TYPE_ARRAY(new ArrayConverter()),
	TYPE_LIST(new ListConverter()),
	TYPE_SET(new SetConverter()),
	TYPE_MAP(new MapConverter()),
	TYPE_BEAN(new BeanConverter()),
	TYPE_DEFAULT(new DefaultConverter<Object>()), 
	TYPE_AMFOBJECT(new AmfObjectConverter()),
	;

	private final IConverter<?> converter;
	private final Class<?>[] types;

	private ConverterType(IConverter<?> converter, Class<?>... types) {
		this.converter = converter;
		this.types = types;
	}

	/**
	 * 将数据转换为目标对象
	 * 
	 * @param value
	 *            原始数据
	 * @param params
	 *            额外参数
	 * @return 目标对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T convert(Object value, Object... params) {
		return (T) converter.convert(value, params);
	}

	/**
	 * 将数据转换为string
	 * 
	 * @param value
	 *            原始数据
	 * @param params
	 *            额外参数
	 * @return 字符串
	 */
	@SuppressWarnings("unchecked")
	public String toString(Object value, Object... params) {
		return ((IConverter<Object>)converter).toString(value, params);
	}

	private static final Map<Class<?>, IConverter<?>> basicConverters = CollectionUtil.newHashMap();

	static {
		for (ConverterType ct : ConverterType.values()) {
			if (ct.types != null) {
				for (Class<?> cls : ct.types) {
					basicConverters.put(cls, ct.converter);
				}
			}
		}
	}

	private static final ConcurrentMap<TypeInfo, IConverter<?>> specialConverters = CollectionUtil.newConcurrentMap();

	/**
	 * 获取数据类型转换对象
	 * 
	 * @param type
	 *            数据类型信息
	 * @return 数据类型转换对象
	 */
	public static IConverter<?> getConverter(TypeInfo type) {
		Class<?> rawClass = type.getRawClass();
		IConverter<?> converter = null;
		if (rawClass != null) {
			converter = basicConverters.get(rawClass);
		}
		if (converter == null) {
			converter = findSpecialConvert(type, null);
		}
		return converter;
	}

	/**
	 * 获取数据类型转换对象
	 * 
	 * @param type
	 *            数据类型信息
	 * @param sp
	 *            如果type是bean中某个属性的类型信息，则asp表示对这个属性的特殊处理
	 * @return 数据类型转换对象
	 */
	public static IConverter<?> getConverter(TypeInfo type, SpecialProperty sp) {
		if (sp == null) {
			return getConverter(type);
		}
		return findSpecialConvert(type, sp);
	}

	/**
	 * 获取数据类型转换对象
	 * 
	 * @param cls
	 *            数据类型class
	 * @return 数据类型转换对象
	 */
	public static IConverter<?> getConverter(Class<?> cls) {
		return getConverter(TypeInfo.getInstance(cls));
	}

	private static IConverter<?> findSpecialConvert(TypeInfo type, SpecialProperty sp) {
		IConverter<?> converter = specialConverters.get(type);
		if (converter == null) {
			Class<?> rawClass = type.getRawClass();
			if (type.isUnsupportedType()) {
				converter = TYPE_DEFAULT.converter;
			} else if (type.isArray()) {
				converter = new SpecialConverter(TYPE_ARRAY.converter, type.getArrayType());
			} else if (type.isList()) {
				List<TypeInfo> args = type.getGenericTypes();
				converter = new SpecialConverter(TYPE_LIST.converter, rawClass, !CollectionUtil.isEmpty(args) ? args.get(0) : null);
			} else if (type.isSet()) {
				List<TypeInfo> args = type.getGenericTypes();
				converter = new SpecialConverter(TYPE_SET.converter, rawClass, !CollectionUtil.isEmpty(args) ? args.get(0) : null);
			} else if (type.isMap()) {
				List<TypeInfo> args = type.getGenericTypes();
				TypeInfo keyType = null, valueType = null;
				if (!CollectionUtil.isEmpty(args)) {
					keyType = args.get(0);
					if (args.size() > 1) {
						valueType = args.get(1);
					}
				}
				converter = new SpecialConverter(TYPE_MAP.converter, rawClass, keyType, valueType);
			} else if (rawClass == Date.class && sp != null) {
				converter = new SpecialConverter(TYPE_DATE.converter, sp.dateTimeFormat());
			} else {
				converter = new SpecialConverter(TYPE_BEAN.converter, rawClass);
			}

			IConverter<?> old = specialConverters.putIfAbsent(type, converter);
			if (old != null) {
				converter = old;
			}
		}
		return converter;
	}

}
