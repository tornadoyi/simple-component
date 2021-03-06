/**
 * created by haitao.yao @ May 11, 2011
 */
package com.simple.kv.storage.cmem.client;

/**
 * @author haitao.yao @ May 11, 2011
 * 
 */
public enum CASResponse {

	/**
	 * Status indicating that the CAS was successful and the new value is stored
	 * in the cache.
	 */
	OK,
	/**
	 * Status indicating the value was not found in the cache (an add operation
	 * may be issued to store the value).
	 */
	NOT_FOUND,
	/**
	 * Status indicating the value was found in the cache, but exists with a
	 * different CAS value than expected. In this case, the value must be
	 * refetched and the CAS operation tried again.
	 */
	EXISTS,

	/**
	 * ERROR
	 */
	ERROR
}
