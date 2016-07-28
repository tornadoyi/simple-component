package com.simple.kv.storage.serialization;

/**
 * 可以实现自定义的特殊序列化规则
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 *
 */
public interface SpecialSerializationHandler {

	/**
	 * 序列化
	 * @param data 需要序列化的对象数据
	 * @return 序列化后的字节数组
	 */
	public byte[] serialize(Object data);
	
	/**
	 * 反序列化
	 * @param bytes 字节数组
	 * @return 反序列化后的对象
	 */
	public Object deserialize(byte[] bytes);
	
}
