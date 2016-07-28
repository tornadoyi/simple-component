package com.simple.kv.storage.serialization;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simple.kv.reflect.TypeInfo;

/**
 * 序列化接口
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public interface ISerializer {
	
	/**
	 * 序列化简单类型（非List、非Set、非Map、非数组，非HeSerializable对象），即HeSerializableType中定义的flag为1~17的类型，如果传入其他类型，将抛出运行时异常HeException
	 * @param data 原始数据
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeSimpleData(Object data);
	
	/**
	 * 序列化简单类型（非List、非Set、非Map、非数组，非HeSerializable对象），即HeSerializableType中定义的flag为1~17的类型，如果传入其他类型，将抛出运行时异常HeException
	 * @param data 原始数据
	 * @param type 数据类型，如果不特别指定（比如smallString之类），则传null即可，实际使用data对象的默认类型
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeSimpleData(Object data, HeSerializableType type);
	
	/**
	 * 序列化实现了HeSerializable接口的对象
	 * @param data
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeHeObject(HeSerializable data);
	
	/**
	 * 序列化需要特殊序列化的对象，使用已注册的自定义序列化处理器
	 * @param data 原始数据
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeSpecialData(Object data);
	
	/**
	 * 序列化需要特殊序列化的对象，使用参数传入的自定义的序列化处理器
	 * @param data 原始数据
	 * @param handler 自定义的序列化处理器
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeSpecialData(Object data, SpecialSerializationHandler handler);
			
	/**
	 * 序列化数组
	 * @param data 数组对象，数组的声明及其元素必须是同一具体类型
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeArray(Object data);
	
	/**
	 * 序列化数组
	 * @param data 数组对象
	 * @param itemType 数组中元素的类型
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeArray(Object data, TypeInfo itemType);
	
	/**
	 * 序列化List
	 * @param data List对象
	 * @param itemType List中元素的类型
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeList(List<?> data, TypeInfo itemType);
	
	/**
	 * 序列化Set
	 * @param data Set对象
	 * @param itemType Set中元素的类型
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeSet(Set<?> data, TypeInfo itemType);
	
	/**
	 * 序列化Map
	 * @param data Map对象
	 * @param keyType key的类型
	 * @param valueType value的类型
	 * @return 序列化后的字节数组
	 */
	public byte[] serializeMap(Map<?, ?> data, TypeInfo keyType, TypeInfo valueType);
	
	/**
	 * 序列化对象，对象的类型必须是在HeSerializableType中定义的类型
	 * @param data
	 * @param typeInfo 对象的类型信息
	 * @return 序列化后的字节数组
	 */
	public byte[] serialize(Object data, TypeInfo typeInfo);


}
