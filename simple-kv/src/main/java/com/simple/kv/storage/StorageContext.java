package com.simple.kv.storage;

import java.util.Map;

import com.simple.base.util.CollectionUtil;



/**
 * 存储上下文，主要在多层存储中，共享一些数据，比如可以避免多次序列化
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 *
 */
class StorageContext {

	private static final ThreadLocal<Map<Object, byte[]>> encodedObject = new ThreadLocal<Map<Object, byte[]>>();
	
	private static Map<Object, byte[]> getEncodedObjectMap() {
		Map<Object, byte[]> map = encodedObject.get();
		if(map == null) {
			map = CollectionUtil.newHashMap();
			encodedObject.set(map);
		}
		return map;
	}
	
	static void setEncodedObject(Object keyParam, byte[] bytes) {
		getEncodedObjectMap().put(keyParam, bytes);
	}
	
	static byte[] getEncodedObject(Object keyParam) {
		return getEncodedObjectMap().get(keyParam);
	}
	
	static void clear() {
		getEncodedObjectMap().clear();
	}
	
	
}
