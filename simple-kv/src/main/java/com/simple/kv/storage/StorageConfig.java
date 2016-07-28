package com.simple.kv.storage;

import com.simple.base.util.tuple.TwoTuple;

/**
 * 存储的配置类
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 *
 */
public interface StorageConfig {

	/** 该配置唯一的key，框架将根据这个key查找到相应的存储配置类，此值必须有 */
	public String getConfigKey();
	
	/** 持久化的数据源名称，对于mysql，对应于db_conf中的instance_name，当允许持久化时，此值必须有 */
	public String getPersistentName();
	
	/** cache group的名称，对应于cache-conf中的相关配置，当允许cache时，此值必须有 */
	public String getCacheGroupName();
	
	/** 是否允许持久化 */
	public boolean enablePersistence();
	
	/** 是否允许存储在cache（持久化层为cmem时，此字段不起作用） */
	public boolean enableCache();

    /** 当且仅当Persistence的Mode为CMEM_CACHE时, LiveTime为CMEM的过期时间**/
    public int getLiveTime();
	
	/** 数据库表名，需要持久化时，此值必须有 */
	public String getTableName();
	
	/** 用于cache及cmem的key的前缀，此值必须有 */
	public String getKeyPrefix();
	
	/** cache的过期时间，秒数 */
	public int getCacheExpireTime();
	
	/** 是否启用分段存储 */
	public boolean enableCacheDivide();
	
	/** 在cache中是否按天过期，当此值为true时，enablePersistence必须为false */
	public boolean isDailyExpired();
	
	/** 当isDailyExpired为true时，需要提供按天过期的北京时间，第一位是小时，第二位是分钟 */
	public TwoTuple<Integer, Integer> getDailyExpiredTime();
	
    /** key-value持久化的数据源名称，对于key-value storage，对应于kv-storage-conf中的instance_name，当使用key-value storage时，此值必须有 */
    public String getKvPersistentName();
	
}
