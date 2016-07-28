package com.simple.kv.storage.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据转换相关工具类
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public class ConverterUtil {

	/**
	 * JavaBean转换为map
	 * 
	 * @param bean
	 * @return 转换后的map，bean的每一个属性名是map的key，每一个属性值是map的value
	 */
	public static Map<?, ?> beanToMap(Object bean) {
		return beanToMap(bean, null);
	}

	/**
	 * JavaBean转换为map
	 * 
	 * @param bean
	 * @param mapClass
	 *            map具体实现类的Class，默认是java.util.HashMap
	 * @return 转换后的map，bean的每一个属性名是map的key，每一个属性值是map的value
	 */
	public static Map<?, ?> beanToMap(Object bean, Class<?> mapClass) {
		return (Map<?, ?>) ConverterType.TYPE_MAP.convert(bean, mapClass);
	}

	/**
	 * 对象转换为能用amf3序列化之后给as使用的对象
	 * 
	 * @param obj
	 *            原始对象
	 * @return
	 */
	public static Object toAmfObject(Object obj) {
		return ConverterType.TYPE_AMFOBJECT.convert(obj);
	}

	/**
	 * map转换为JavaBean
	 * 
	 * @param map
	 * @param beanClass
	 *            bean的Class
	 * @return 转换后的bean，map的每一个key-value对应于bean中的每一个属性名-属性值
	 */
	@SuppressWarnings("unchecked")
	public static <T> T mapToBean(Map<?, ?> map, Class<T> beanClass) {
		return (T) ConverterType.TYPE_BEAN.convert(map, beanClass);
	}

	/**
	 * 对象转换为可读的String
	 * 
	 * @param obj
	 *            支持基本类型、String、Date、List、Map、数组、JavaBean
	 * @return 转换后的String
	 */
	@SuppressWarnings("unchecked")
	public static String objectToString(Object obj) {
		if (obj == null) {
			return "null";
		}
		return ((IConverter<Object>) ConverterType.getConverter(obj.getClass())).toString(obj);
	}

	/**
	 * JavaBean之间的copy
	 * 
	 * @param src
	 *            原始bean
	 * @param destClass
	 *            目标bean的class
	 * @return 返回目标bean，其中的每个属性是从原始bean中的每个同名属性转换而来
	 */
	@SuppressWarnings("unchecked")
	public static <T> T beanCopy(Object src, Class<T> destClass) {
		return (T) ConverterType.TYPE_BEAN.convert(src, destClass);
	}

	/**
	 * JavaBean之间的copy
	 * 
	 * @param src
	 *            原始bean
	 * @param dest
	 *            目标bean
	 * @return 返回目标bean，其中的每个属性是从原始bean中的每个同名属性转换而来
	 */
	@SuppressWarnings("unchecked")
	public static <T> T beanCopy(Object src, Object dest) {
		return (T) ConverterType.TYPE_BEAN.convert(src, dest);
	}

	/**
	 * list copy
	 * @param srcList 源list
	 * @param destElementClass 目标list的元素类型
	 * @return
	 */
	public static <T> List<T> listCopy(List<?> srcList, Class<T> destElementClass) {
	    if(srcList == null) {
	        return null;
	    }
		List<T> ret = new ArrayList<T>(srcList.size());
		for (Object src : srcList) {
			ret.add(beanCopy(src, destElementClass));
		}
		return ret;
	}

}
