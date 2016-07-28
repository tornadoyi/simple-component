package com.simple.kv.storage;

import java.lang.reflect.Method;

import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.error.KVException;

/**
 * 操作对象的构建工厂
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public interface OperationFactory {

	public static final IOperation NONE_OPERATION = new IOperation() {
		@Override
		public Object execute(Object[] args) {
			throw new KVException( "none");
		}
		@Override
		public void setResultItemType(TypeInfo typeInfo) {
			throw new KVException("none");
		}
	};
	
	/**
	 * 创建一个操作对象
	 * 
	 * @param info 存储基础信息
	 * @param method 存储的声明方法
	 * @return 操作对象
	 */
	public IOperation getOperation(StorageInfo info, Method method);
	
	/**
	 * 创建指定类型的操作对象
	 * @param type 操作类型
	 * @param info 存储基础信息
	 * @param method 存储的声明方法
	 * @return
	 */
	public IOperation getOperation(OperationType type, StorageInfo info, Method method);

}
