package com.simple.kv.storage.serialization;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.error.KVException;

/**
 * 可以被序列化的类型枚举，有一些类型是java语言中本身不具备的
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public enum HeSerializableType {

	/** 没有标注特殊type的情况 */
	DEFAULT(0, null),

	/* ----------------- 下面是适用于简单序列化的类型 ------------------------- */

	/** byte类型 */
	BYTE(1, ConverterType.TYPE_BYTE, byte.class, Byte.class),
	/** short类型 */
	SHORT(2, ConverterType.TYPE_SHORT, short.class, Short.class),
	/** int类型 */
	INT(3, ConverterType.TYPE_INTEGER, int.class, Integer.class),
	/** long类型 */
	LONG(4, ConverterType.TYPE_LONG, long.class, Long.class),
	/** boolean类型 */
	BOOLEAN(5, ConverterType.TYPE_BOOLEAN, boolean.class, Boolean.class),
	/** float类型 */
	FLOAT(6, ConverterType.TYPE_FLOAT, float.class, Float.class),
	/** double类型 */
	DOUBLE(7, ConverterType.TYPE_DOUBLE, double.class, Double.class),
	/** char类型 */
	CHAR(8, ConverterType.TYPE_CHARACTER, char.class, Character.class),
	/** 大小在-128~127之间的整数，占用空间为1字节 */
	TINY_INT(9, ConverterType.TYPE_INTEGER),
	/** 大小在-32768~32767之间的整数，占用空间为2字节 */
	SMALL_INT(10, ConverterType.TYPE_INTEGER),
	/** 大小在-8388608~8388607之间的整数，占用空间为3字节 */
	MEDIUM_INT(11, ConverterType.TYPE_INTEGER),
	/** 大小在0~255之间的整数，占用空间为1字节 */
	UNSIGNED_TINY_INT(12, ConverterType.TYPE_INTEGER),
	/** 大小在0~65535之间的整数，占用空间为2字节 */
	UNSIGNED_SMALL_INT(13, ConverterType.TYPE_INTEGER),
	/** 大小在0~16777215之间的整数，占用空间为3字节 */
	UNSIGNED_MEDIUM_INT(14, ConverterType.TYPE_INTEGER),
	/** 根据UTF-8编码得到的byte数组长度不超过65535的字符串 */
	STRING(15, ConverterType.TYPE_STRING, true, String.class),
	/** 日期时间类型，java.util.Date */
	DATE(17, ConverterType.TYPE_DATE, true, Date.class),

	/* -----------------上面是适用于简单序列化的类型，下面是适用于复杂序列化的类型---------------------- */

	/** 容量不超过65535且元素不为null的数组，只支持单一类型的数组，即数组元素的具体类型必须相同，且包含于HeSerializableType枚举中，数组类型的声明(ComponentType)不能是接口、抽象类、基类 */
	ARRAY(51, null, true),
	/** 容量不超过65535且元素不为null的列表，要求List的声明必须包含强类型的泛型，泛型的类型不能是接口、抽象类、基类，List中的元素类型必须包含于HeSerializableType枚举中 */
	LIST(52, null, true, List.class),
	/** 容量不超过65535且元素不为null的不包含重复元素的集合，要求Set的声明必须包含强类型的泛型，泛型的类型不能是接口、抽象类、基类，Set中的元素类型必须包含于HeSerializableType枚举中 */
	SET(53, null, true, Set.class),
	/** 容量不超过65535且key及value不为null的键值对映射，要求Map的声明必须包含key及value的强类型泛型，泛型的类型不能是接口、抽象类、基类 ，Map中的key及value类型必须包含于HeSerializableType枚举中 */
	MAP(54, null, true, Map.class),
	/** 实现了HeSerializable接口的一般对象 */
	HE_SERIALIZABLE_OBJECT(55, null, true, HeSerializable.class),
	/** null */
	NULL(56, null, true),
	/** 当出现循环引用时做特殊处理用途 */
	REF(57, null, true),
	/** 特殊的对象，需要特殊实现序列化机制 */
	SPECIAL_OBJECT(58, null, true), ;

	/** 范围 0~127 */
	private byte flag;
	/** 默认能处理的Class */
	private Class<?>[] clss;
	/** 目标对象的转换适配器 */
	private ConverterType converterType;
	/** 这种类型是否可以为null */
	private boolean canBeNull;

	private HeSerializableType(int flag, ConverterType converterType, Class<?>... clss) {
		this.flag = (byte) flag;
		this.converterType = converterType;
		this.clss = clss;
	}

	private HeSerializableType(int flag, ConverterType converterType, boolean canBeNull, Class<?>... clss) {
		this.flag = (byte) flag;
		this.converterType = converterType;
		this.clss = clss;
		this.canBeNull = canBeNull;
	}

	/** 取得类型标识 */
	public byte getFlag() {
		return flag;
	}

	/** 取得目标对象转换器 */
	public ConverterType getConverterType() {
		return converterType;
	}

	/** 是否可以为空 */
	public boolean canBeNull() {
		return canBeNull;
	}

	/** 是否适用于简单序列化的类型 */
	public boolean isSimpleType() {
		return flag < 50;
	}

	private static final Map<Byte, HeSerializableType> flagMap = CollectionUtil.newCopyOnWriteHashMap();
	private static final Map<Class<?>, HeSerializableType> classMap = CollectionUtil.newConcurrentMap();

	static {
		for (HeSerializableType ht : HeSerializableType.values()) {
			HeSerializableType oht;
			if ((oht = flagMap.put(ht.flag, ht)) != null) {
				throw new Error("repeat flag " + ht.flag + " in HeSerializableType " + oht.name() + " and " + ht.name());
			}
			if (ht.clss != null) {
				for (Class<?> cls : ht.clss) {
					if ((oht = classMap.put(cls, ht)) != null) {
						throw new Error("repeat class " + cls + " in HeSerializableType " + oht.name() + " and " + ht.name());
					}
				}
			}
		}
	}

	/**
	 * 根据标识获取相应的类型枚举
	 * 
	 * @param flag
	 *            标识
	 * @return 类型枚举
	 */
	static HeSerializableType getHeSerializableType(byte flag) {
		if (flagMap.containsKey(flag)) {
			return flagMap.get(flag);
		} else {
			throw new KVException("NOT_SUPPORTED_TYPE serializable type flag " + flag);
		}
	}

	/**
	 * 得到Class默认的序列化类型
	 * 
	 * @param cls
	 *            Class
	 * @return 如果该Class不是可被支持的序列化类型，则抛出运行时异常HeException
	 */
	static HeSerializableType getHeSerializableType(Class<?> cls) {
		HeSerializableType type = classMap.get(cls);
		if (type == null) {
			synchronized (cls) {
				type = classMap.get(cls);
				if (type == null) {
					if (cls.isArray()) {
						type = ARRAY;
					} else if (cls == Collection.class) {
						type = LIST;
					} else if (SpecialSerializationHandlerFactory.containsHandler4ObjectClass(cls)) {
						type = SPECIAL_OBJECT;
					} else {
						for (Map.Entry<Class<?>, HeSerializableType> entry : classMap.entrySet()) {
							if (entry.getKey().isAssignableFrom(cls)) {
								type = entry.getValue();
								break;
							}
						}
					}
					if (type != null) {
						classMap.put(cls, type);
					} else {
						throw new KVException(cls.getName() + "NOT_SUPPORTED_TYPE serializing");
					}
				}
			}
		}
		return type;
	}

	/**
	 * 得到符合某种typeInfo要求的序列化类型
	 * 
	 * @param typeInfo
	 * @return 如果不是可被支持的序列化类型，则抛出运行时异常KVException
	 */
	static HeSerializableType getHeSerializableType(TypeInfo typeInfo) {
		if (typeInfo.isUnsupportedType()) {
			throw new KVException("NOT_SUPPORTED_TYPE" + typeInfo);
		}
		if (typeInfo.isArray()) {
			return ARRAY;
		} else {
			return getHeSerializableType(typeInfo.getRawClass());
		}
	}

	public static void main(String[] args) {

	}

}
