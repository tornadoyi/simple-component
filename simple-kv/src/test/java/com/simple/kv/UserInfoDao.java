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
	public static final String KV_PERSISTENT_NAME = "cmem-2";
	
	@Override
	public String getKvPersistentName() {
		return KV_PERSISTENT_NAME;
	}

	
	
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


	
	

}
