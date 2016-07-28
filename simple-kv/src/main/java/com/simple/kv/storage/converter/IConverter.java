package com.simple.kv.storage.converter;



/**
 * 数据类型转换接口
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 *
 * @param <T>
 */
public interface IConverter<T> {
	
	/**
	 * 将数据转换为目标对象
	 * @param value 原始数据
	 * @param params 额外参数
	 * @return	目标对象
	 */
	public T convert(Object value, Object... params);
	
	/**
	 * 将数据转换为string
	 * @param value 原始数据
	 * @param params 额外参数
	 * @return 字符串
	 */
	public String toString(T value, Object... params);
	
}
