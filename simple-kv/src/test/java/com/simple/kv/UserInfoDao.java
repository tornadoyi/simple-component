/**
 * 
 */
package com.simple.kv;

import com.simple.base.util.tuple.TwoTuple;
import com.simple.kv.storage.OperationType;
import com.simple.kv.storage.anno.PValue;
import com.simple.kv.storage.anno.Pkey;
import com.simple.kv.storage.anno.Storage;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月20日
 */
public class UserInfoDao extends BaseDao {

	public static final String STORAGE_CONFIG_KEY = "UserInfoDao";
	public static final String PERSISTENT_NAME_DB_1 = "1";
	
	
	@Storage(storageConfigKey = STORAGE_CONFIG_KEY, type = OperationType.SET)
	public boolean set(@Pkey({ "id" }) long id, @PValue UserInfo value) {
		return false;
	}
	
	@Storage(storageConfigKey = STORAGE_CONFIG_KEY, type = OperationType.SET)
	public boolean set(@Pkey({ "name", "level" }) TwoTuple<String, Integer> key, @PValue UserInfo value) {
		return false;
	}
	
	@Storage(storageConfigKey = STORAGE_CONFIG_KEY, type = OperationType.GET)
	public UserInfo get(@Pkey({"id"}) long id){
		return null;
	}
	
	
	
	@Storage(storageConfigKey = STORAGE_CONFIG_KEY, type = OperationType.GET)
	public UserInfo get(@Pkey({ "name", "level" }) TwoTuple<String, Integer> key) {
		return null;
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.simple.kv.storage.StorageConfig#getPersistentName()
	 */
	@Override
	public String getPersistentName() {
		return PERSISTENT_NAME_DB_1;
	}

	/* (non-Javadoc)
	 * @see com.simple.kv.storage.StorageConfig#getCacheGroupName()
	 */
	@Override
	public String getCacheGroupName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.simple.kv.storage.StorageConfig#enablePersistence()
	 */
	@Override
	public boolean enablePersistence() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.simple.kv.storage.StorageConfig#getTableName()
	 */
	@Override
	public String getTableName() {
		return "user_info";
	}

}
