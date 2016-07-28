/**
 * $Id: CasUpdate.java 438800 2014-04-04 03:23:10Z yongliang.zhao $
 * Copyright(C) 2010-2016 happyelements.com. All rights reserved.
 */
package com.simple.kv.storage;

import com.simple.kv.storage.error.KVException;


/**
 * 
 * ！！！！！！注意：此操作仅适用于无论如何我必须更新此对象时
 * 
 * 此对象封装了一个cas的操作。操作的过程为：通过getCasResult取出object,version
 * 然后通过updateObjectValue方法更新对象的内容
 * 最后通过第一步的verion值以及对象去进行持久化
 * 如果遇到version失效，则进行重复操作，一共进行10次(次数可更改，一般认为超过10次的存在其他逻辑问题)
 * 如果除update是否成功之外，还需要其他的返回值，请使用CasUpdateWithReturn
 * 
 * @author <a href="mailto:yongliang.zhao@happyelements.com">yongliang.zhao</a>
 * @version 1.1
 * @since 1.0
 */
public abstract class CasUpdate<T> {

    protected T value;
    
	@SuppressWarnings("unchecked")
	public boolean update() {
		for (int i = 0; i < 10; i++) { // cas重试机制，自动重试10次
			CasResult<T> casResult = getObjectForCas(); // 获取casResult
			if (casResult == null) {
				casResult = (CasResult<T>) CasResult.NOT_FOUND_RESULT;
			}
			long version = casResult.getVersion();
			value = (T) casResult.getValue();
			value = changeObjectValue(value); // 更新value对象，上层需要实现changeObjectValue(T value)方法。当存储媒介不存在记录时，value为null
			if (value == null) { // 上层逻辑发现不需要改变时，返回false终止逻辑
				return false;
			}
			CasStatus status = storeWithCas(value, version); // 当version为-1(不存在记录特殊标记version值)，底层会自动转换成set操作
			if (status == CasStatus.OK) {
				return true;
			}
		}
		throw new KVException("CAS_VERSION_INVALID");
	}

	/**
	 * 在有些情况下，cas操作完成后，调用者希望知道最终被存储的对象是什么，
	 * 这个方法将返回被cas成功存储的对象值。
	 * 
	 * @author <a href="mailto:weibo.li@happyelements.com">weibo.li</a>
	 * @return 被cas成功存储的对象值
	 */
	public T getValue() {
	    return value;
	}
	
	/**
	 * 通过version来实现持久化的有效更新
	 * 
	 * @param object
	 * @param version
	 * @return
	 */
	public abstract CasStatus storeWithCas(T value, long version);

	/**
	 * 更新Objec对象的内容，例如增加银币等
	 * 需要注意：当存储媒介不存在此记录时,value为null，上层逻辑需要手动赋值，赋值之后，CAS操作会根据version的特殊标记值，
	 * 自动转换成set操作
	 * 
	 * @param value
	 * @return
	 */
	public abstract T changeObjectValue(T value);

	/**
	 * 获取CasResult，包含Objec对象以及version(casId)
	 * 当持久化媒介不存在记录时，返回特殊CasResult，其中value为null，version为-1(记录不存在特殊标记值).
	 * 
	 * @return
	 */
	public abstract CasResult<T> getObjectForCas();

}
