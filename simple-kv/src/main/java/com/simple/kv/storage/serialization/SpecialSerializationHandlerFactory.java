package com.simple.kv.storage.serialization;

import java.util.Map;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.storage.error.KVException;

public class SpecialSerializationHandlerFactory {

	private static final Map<Class<?>, SpecialSerializationHandler> specialHandlerMap = CollectionUtil.newCopyOnWriteHashMap();

	/**
	 * 注册对特定对象的特殊序列化处理器
	 * 
	 * @param clazz
	 *            特定对象的Class
	 * @param handler
	 *            自定义的特殊序列化处理器
	 */
	public static void registerHandler(Class<?> clazz, SpecialSerializationHandler handler) {
		specialHandlerMap.put(clazz, handler);
	}

	/**
	 * 得到特定对象的特殊序列化处理器，在没有注册处理器的情况下，会抛出运行时异常
	 * 
	 * @param clazz
	 *            特定对象的Class
	 * @return 自定义的特殊序列化处理器
	 */
	public static SpecialSerializationHandler getSpecialHandler(Class<?> clazz) {
		SpecialSerializationHandler handler = specialHandlerMap.get(clazz);
		if (handler == null) {
			throw new KVException("SOMETHING_NOT_FOUND SpecialSerializationHandler of " + clazz.getName());
		}
		return handler;
	}
	
	/**
	 * 判定是否有注册的objClass的handler
	 * @param objClass
	 * @return
	 */
	static boolean containsHandler4ObjectClass(Class<?> objClass) {
		return specialHandlerMap.containsKey(objClass);
	}

}
