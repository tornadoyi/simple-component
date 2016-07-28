/**
 * created by haitao.yao @ May 10, 2011
 */
package com.simple.kv.storage.cmem.client;

import java.util.Collection;
import java.util.Map;

/**
 * interface for kv storage
 * 
 * @author haitao.yao @ May 10, 2011
 * 
 */
public interface KVStorageClient {
	
	/**
	 * expiration, a second
	 */
	public static final int EXPIRE_SECOND = 1;

	/**
	 * expiration, a minute
	 */
	public static final int EXPIRE_MINUTE = 60;
	
	/**
	 * expiration, a hour
	 */
	public static final int EXPIRE_HOUR = 60 * EXPIRE_MINUTE ;
	
	/**
	 * expiration, a day
	 */
	public static final int EXPIRE_DAY = 24 * EXPIRE_HOUR;

	/**
	 * max expiration
	 */
	public static final int EXPIRE_MAX = EXPIRE_DAY * 30;
	
	/**
	 * never expiration: mean you not use the expire function
	 */
	public static final int EXPIRE_NEVER = 0;
	
	/**
	 * mget like memcached
	 * 
	 * @param keys
	 * @return
	 */
	public abstract Map<String, Object> getBulk(Collection<String> keys);
	
	/**
	 * mget like memcached, and can detect error
	 * 
	 * @param keys
	 * @return
	 */
	public abstract Map<String, Object> getBulkExt(Collection<String> keys);

	/**
	 * get the value
	 * 
	 * @param key
	 * @return
	 */
	public abstract Object get(String key);
	
	/**
	 * get the value and can detect error
	 * 
	 * @param key
	 * @return
	 */
	public abstract Object getExt(String key);

	/**
	 * decrement the value
	 * 
	 * @param key
	 * @param inc
	 *            增量
	 * @param def
	 *            如果不存在后的默认值
	 * @return
	 */
	public abstract long decr(String key, int inc, long def);

	/**
	 * increment a value
	 * 
	 * @param key
	 * @param inc
	 * @param def
	 *            如果不存在后的默认值
	 * @return
	 */
	public abstract long incr(String key, int inc, long def);

	/**
	 * @param key
	 * @param value
	 * @param duration
	 *            unit: second
	 * @return
	 */
	public abstract boolean add(String key, Object value);

	/**
	 * 
	 * @param key
	 * @param value
	 * @param expire unit:second
	 * @return
	 */
	public abstract boolean add(String key, Object value, int expire);
	
	/**
	 * @param key
	 * @param value
	 * @param duration
	 *            unit: second
	 * @return
	 */
	public abstract boolean set(String key, Object value);

	/**
	 * 
	 * @param key
	 * @param value
	 * @param expire unit:second
	 * @return
	 */
	public abstract boolean set(String key, Object value, int expire);
	/**
	 * delete the value
	 * 
	 * @param key
	 * @return
	 */
	public abstract boolean delete(String key);

	/**
	 * initialize the counter
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public abstract boolean storeCounter(String key, long value);

	/**
	 * get the value as counter
	 * <p>
	 * same as incr(key, 1)
	 * 
	 * @param key
	 * @return
	 */
	long getCounter(String key);
	
	/**
	 * gets for the key
	 * 
	 * @param key
	 * @return
	 */
	public CASValue gets(String key);
	
	/**
	 * gets for the key, and can detect error
	 * 
	 * @param key
	 * @return
	 */
	public CASValue getsExt(String key);

	/**
	 * cas for the memcached
	 * 
	 * @param key
	 *            the key
	 * @param casId
	 *            the casId
	 * @param value
	 *            the value to set
	 * @return
	 */
	CASResponse cas(String key, long casId, Object value);
	
	/**
	 * 
	 * @param key
	 * @param casId
	 * @param value
	 * @param expire unit:second
	 * @return
	 */
	CASResponse cas(String key, long casId, Object value, int expire);

}
