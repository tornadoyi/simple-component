/**
 * 
 */
package com.simple.kv;

import com.simple.base.util.DateTimeUtil;
import com.simple.base.util.tuple.Tuple;
import com.simple.base.util.tuple.TwoTuple;
import com.simple.kv.storage.HeStorageFactory;
import com.simple.kv.storage.PersistentMode;
import com.simple.kv.storage.StorageConfig;
import com.simple.kv.storage.logger.KVLogger;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月20日
 */
public abstract class BaseDao implements StorageConfig {

	public static final String CMEM_INSTANCE_NAME = "default";
	
	private String keyPrefix;
	
	public BaseDao() {
		this.keyPrefix = getClass().getSimpleName().split("\\u0024")[0];
		KVLogger.getLogger().info("regist storage:" + getConfigKey() + " start");
		StorageConfig[] config = { this };
		HeStorageFactory.registStorageConfig(config);
		System.out.println(HeStorageFactory.getStorageConfig(this.getConfigKey()));
		HeStorageFactory.registPersistentMode(getPersitentModel(), this);
		KVLogger.getLogger().info("regist storage:" + getConfigKey() + " done");
	}
	
	
	/**
	 * 存储模式
	 * 
	 * @return
	 */
	public PersistentMode getPersitentModel() {
		return PersistentMode.CMEM;
	}
	
	@Override
	public String getConfigKey() {
		return keyPrefix;
	}

	@Override
	public String getKeyPrefix() {
		return keyPrefix;
	}
	
	@Override
	public boolean enableCacheDivide() {
		return false;
	}
	
	@Override
	public String getKvPersistentName() {
		return CMEM_INSTANCE_NAME;
	}
	
	@Override
	public int getLiveTime() {
		return 0;
	}

	@Override
	public int getCacheExpireTime() {
		return DateTimeUtil.DAY_SECOND * 29;
	}

	@Override
	public boolean isDailyExpired() {
		return false;
	}

	@Override
	public boolean enableCache() {
		return false;
	}

	@Override
	public TwoTuple<Integer, Integer> getDailyExpiredTime() {
		return Tuple.tuple(4, 0);
	}


}
