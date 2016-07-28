package com.simple.kv.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.simple.kv.storage.error.KVException;

/**
 * 反射相关的util方法
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public class ReflectUtil {

	private static final Map<String, Method> methodCache = new ConcurrentHashMap<String, Method>();

	/**
	 * 获取类中的方法
	 * 
	 * @param clazz
	 *            类的Class
	 * @param methodName
	 *            方法名
	 * @param parameterTypes
	 *            参数声明
	 * @return 方法
	 * @throws NoSuchMethodException
	 *             没有这个方法时抛出异常
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		StringBuilder sb = new StringBuilder();
		sb.append(clazz.getName()).append(".").append(methodName);
		if (parameterTypes != null) {
			for (Class<?> pcls : parameterTypes) {
				sb.append(".").append(pcls.getName());
			}
		}
		String key = sb.toString();
		Method method = methodCache.get(key);
		if (method == null) {
			synchronized (key.intern()) {
				method = methodCache.get(key);
				if (method == null) {
					method = clazz.getDeclaredMethod(methodName, parameterTypes);
					methodCache.put(key, method);
				}
			}
		}
		return method;
	}

	private static final Map<Class<?>, Constructor<?>> constructorCache = new ConcurrentHashMap<Class<?>, Constructor<?>>();

	/**
	 * 创造某个类的实例
	 * 
	 * @param clazz
	 *            类的Class
	 * @return 类的实例
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createInstance(Class<T> clazz) {
		Constructor<T> c = (Constructor<T>) constructorCache.get(clazz);
		if (c == null) {
			synchronized (clazz) {
				c = (Constructor<T>) constructorCache.get(clazz);
				if (c == null) {
					try {
						c = (Constructor<T>) clazz.getDeclaredConstructor();
						if (!c.isAccessible()) {
							c.setAccessible(true);
						}
						constructorCache.put(clazz, c);
					} catch (Exception e) {
						throw new KVException("create instance failed" + clazz.getName(), e);
					}
				}
			}
		}
		try {
			return c.newInstance();
		} catch (Exception e) {
			throw new KVException("create instance failed" + clazz.getName(), e);
		}
	}
	
}
