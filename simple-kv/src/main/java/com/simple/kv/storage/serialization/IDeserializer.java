package com.simple.kv.storage.serialization;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simple.kv.reflect.TypeInfo;

/**
 * 反序列化接口
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public interface IDeserializer {
	
	
	/**
	 * 反序列化简单类型（非List、非Set、非Map、非数组，非HeSerializable对象），即HeSerializableType中定义的flag为1~17的类型，如果是其他类型，将抛出运行时异常HeException
	 * @param bytes 字节数组
	 * @return 反序列化后的结果
	 */
	public <T> T deserializeSimpleData(byte[] bytes);
	
	/**
	 * 反序列化简单类型（非List、非Set、非Map、非数组，非HeSerializable对象），即HeSerializableType中定义的flag为1~17的类型，如果是其他类型，将抛出运行时异常HeException
	 * @param bytes 字节数组
	 * @param type 目标类型，会尝试将反序列化后的结果转换为此类型
	 * @return 反序列化后的结果
	 */
	public <T> T deserializeSimpleData(byte[] bytes, HeSerializableType type);
	
	/**
	 * 反序列化实现了HeSerializable接口的对象
	 * @param bytes 字节数组
	 * @param targetClass 目标对象的Class
	 * @return 反序列化后的对象
	 */
	public <T extends HeSerializable> T deserializeHeObject(byte[] bytes, Class<T> targetClass);
	
	/**
	 * 反序列化特殊对象，使用已注册的自定义处理器
	 * @param bytes 字节数组
	 * @param targetClass 目标对象的Class，可根据此Class查找到对应的自定义处理器
	 * @return 反序列化后的对象
	 */
	public <T> T deserializeSpecialData(byte[] bytes, Class<T> targetClass);
	
	/**
	 * 反序列化特殊对象，使用参数传入的自定义处理器
	 * @param bytes 字节数组
	 * @param handler 自定义的特殊处理器
	 * @return 反序列化后的对象
	 */
	public <T> T deserializeSpecialData(byte[] bytes, SpecialSerializationHandler handler);
			
	/**
	 * 反序列化数组
	 * @param bytes 字节数组
	 * @param arrayClass 数组的Class，数组的元素声明必须是具体类型
	 * @return 反序列化后的数组对象
	 */
	public Object deserializeArray(byte[] bytes, Class<?> arrayClass);
	
	/**
	 * 反序列化数组
	 * @param bytes 字节数组
	 * @param itemType 数组中元素的类型
	 * @return 反序列化后的数组对象
	 */
	public Object deserializeArray(byte[] bytes, TypeInfo itemType);
	
	/**
	 * 反序列化List
	 * @param bytes 字节数组
	 * @param itemType List中元素的类型
	 * @param targetClass 可选，指明List的具体实现类型，传入null则使用java.util.ArrayList
	 * @return 反序列化后的List对象
	 */
	public <T> List<T> deserializeList(byte[] bytes, TypeInfo itemType, Class<?> targetClass);
	
	/**
	 * 反序列化List
	 * @param bytes 字节数组
	 * @param itemType List中元素的类型
	 * @return 反序列化后的List对象
	 */
	public <T> List<T> deserializeList(byte[] bytes, TypeInfo itemType);
	
	/**
	 * 反序列化Set
	 * @param bytes 字节数组
	 * @param itemType Set中元素的类型
	 * @param targetClass 可选，指明Set的具体实现类型，传入null则使用java.util.HashSet
	 * @return 反序列化后的Set对象
	 */
	public <T> Set<T> deserializeSet(byte[] bytes, TypeInfo itemType, Class<?> targetClass);
	
	/**
	 * 反序列化Set
	 * @param bytes 字节数组
	 * @param itemType Set中元素的类型
	 * @return 反序列化后的Set对象
	 */
	public <T> Set<T> deserializeSet(byte[] bytes, TypeInfo itemType);
	
	/**
	 * 反序列化Map
	 * @param bytes 字节数组
	 * @param keyType key的类型
	 * @param valueType value的类型
	 * @param targetClass 可选，指明Map的具体实现类型，传入null则使用java.util.HashMap
	 * @return 反序列化后的Map对象
	 */
	public <K, V> Map<K, V> deserializeMap(byte[] bytes, TypeInfo keyType, TypeInfo valueType, Class<?> targetClass);
	
	/**
	 * 反序列化Map
	 * @param bytes 字节数组
	 * @param keyType key的类型
	 * @param valueType value的类型
	 * @return 反序列化后的Map对象
	 */
	public <K, V> Map<K, V> deserializeMap(byte[] bytes, TypeInfo keyType, TypeInfo valueType);
	
	/**
	 * 反序列化对象
	 * @param bytes 字节数组
	 * @param typeInfo 对象类型信息
	 * @return 反序列化后的对象
	 */
	public <T> T deserialize(byte[] bytes, TypeInfo typeInfo);
	
}
