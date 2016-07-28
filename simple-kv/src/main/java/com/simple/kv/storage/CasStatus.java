package com.simple.kv.storage;

/**
 * CAS操作的返回状态
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public enum CasStatus {

	/**
	 * Status indicating that the CAS was successful and the new value is stored.
	 */
	OK,
	
	/**
	 * Status indicating the value was not found.
	 */
	NOT_FOUND,
	
	/**
	 * Status indicating the value was found, but exists with a
	 * different CAS value than expected. In this case, the value must be
	 * refetched and the CAS operation tried again.
	 */
	VERSION_INVALID;

}
