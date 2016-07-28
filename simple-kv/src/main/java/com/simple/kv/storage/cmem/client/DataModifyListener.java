/**
 * created by haitao.yao @ May 12, 2011
 */
package com.simple.kv.storage.cmem.client;

/**
 * data modification listener
 * 
 * @author haitao.yao @ May 12, 2011
 * 
 */
public interface DataModifyListener {

	/**
	 * date operations
	 * 
	 * @author haitao.yao @ May 12, 2011
	 * 
	 */
	public static enum Operation {
		INCR, DECR, ADD, REPLACE, SET, DELETE, CAS, STORE_COUNTER
	}

	/**
	 * notify for data change
	 * 
	 * @param key
	 * @param operation
	 */
	void dataModified(String key, Operation operation);

	
}
