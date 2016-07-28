package com.simple.kv.storage;

import com.simple.kv.reflect.TypeInfo;

/**
 * 操作对象接口
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public interface IOperation {

	/**
	 * 执行方法
	 * 
	 * @param args
	 *            参数
	 * @return 结果
	 */
	public Object execute(Object[] args);

	/**
	 * 可以设置结果的类型（对于集合来说，是里面元素的类型）
	 * @param typeInfo
	 */
	public void setResultItemType(TypeInfo typeInfo);
}
