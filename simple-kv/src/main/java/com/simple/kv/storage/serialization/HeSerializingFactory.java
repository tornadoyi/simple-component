package com.simple.kv.storage.serialization;

/**
 * 得到序列化器和反序列化器的工厂
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 *
 */
public class HeSerializingFactory {

	private static final ISerializer serializer = new HeSerializer();
	private static final IDeserializer deserializer = new HeDeserializer();
	
	/**
	 * @return 得到序列化器
	 */
	public static ISerializer getSerializer() {
		return serializer;
	}
	
	/**
	 * @return 得到反序列化器
	 */
	public static IDeserializer getDeserializer() {
		return deserializer;
	}
	
	
}
