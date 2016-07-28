package com.simple.kv.storage;

import com.simple.base.util.tuple.TwoTuple;

/**
 * 用于存储操作的基础信息
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public class StorageInfo {

	/** 存储配置 */
	StorageConfig sc;
	
	/**
	 * 操作类型
	 */
	OperationType type;

	/**
	 * keyParam的信息，二元组中的Integer是方法参数中作为keyParam的参数索引，String[]是mysql中对应的列名
	 */
	TwoTuple<Integer, String[]> keyParamInfo;

	/**
	 * 方法参数中作为value的参数索引，-1表示无value参数
	 */
	int valueParamIndex = -1;

	/**
	 * 方法参数中作为cas版本号的参数索引，-1表示无此参数
	 */
	int casVersionIndex = -1;
	
	public StorageConfig getStorageConfig() {
		return sc;
	}

	public String getPersistentName() {
		return sc.getPersistentName();
	}

	public String getCacheGroupName() {
		return sc.getCacheGroupName();
	}

	public boolean enablePersistence() {
		return sc.enablePersistence();
	}

	public boolean enableCache() {
		return sc.enableCache();
	}

	public String getTableName() {
		return sc.getTableName();
	}

	public String getKeyPrefix() {
		return sc.getKeyPrefix();
	}

	public int getCacheExpireTime() {
		return sc.getCacheExpireTime();
	}
	
	public boolean enableCacheDivide() {
		return sc.enableCacheDivide();
	}

	public boolean isDailyExpired() {
		return sc.isDailyExpired();
	}

	public TwoTuple<Integer, Integer> getDailyExpiredTime() {
		return sc.getDailyExpiredTime();
	}

	public OperationType getType() {
		return type;
	}

	public TwoTuple<Integer, String[]> getKeyParamInfo() {
		return keyParamInfo;
	}

	public int getValueParamIndex() {
		return valueParamIndex;
	}

	public int getCasVersionIndex() {
		return casVersionIndex;
	}


}
