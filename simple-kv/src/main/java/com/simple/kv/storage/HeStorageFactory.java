package com.simple.kv.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import net.sf.cglib.proxy.Enhancer;

import com.simple.base.util.CollectionUtil;
import com.simple.base.util.StringUtil;
import com.simple.base.util.tuple.TwoTuple;
import com.simple.kv.storage.consts.HeConsts;
import com.simple.kv.storage.error.KVException;

/**
 * storage工厂
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public class HeStorageFactory {

	private static final Map<Class<?>, Object> proxyMap = CollectionUtil.newConcurrentMap();
	private static final Map<String, StorageConfig> scMap = CollectionUtil.newConcurrentMap();

	/** 持久化默认使用mysql */
	private static volatile PersistentMode persistentMode = PersistentMode.CMEM;

	/**
	 * 注册持久化模式，如果不注册的话，默认是mysql
	 * 
	 * @param mode
	 */
	public static void registPersistentMode(PersistentMode mode) {
		persistentMode = mode;
	}
	
	private static final Map<StorageConfig, PersistentMode> specialModeMap = CollectionUtil.newConcurrentMap();
	
	/**
	 * 为指定的存储项注册持久化模式，比如在腾讯下注册了cmem模式，但有少量存储项需要使用db，则可用此方法注册
	 * @param mode 持久化模式
	 * @param scs 存储项
	 */
	public static void registPersistentMode(PersistentMode mode, StorageConfig... scs) {
		for(StorageConfig sc : scs) {
			specialModeMap.put(sc, mode);
		}
	}
	
	/**
	 * 得到当前的持久化模式
	 * @param sc 存储项
	 * @return
	 */
	public static PersistentMode getCurrentPersistentMode(StorageConfig sc) {
		return specialModeMap.containsKey(sc) ? specialModeMap.get(sc) : persistentMode;
	}
	
	/**
	 * 注册存储配置
	 * 
	 * @param scs
	 */
	public synchronized static void registStorageConfig(Collection<StorageConfig> scs) {
		registStorageConfig(scs.toArray(new StorageConfig[0]));
	}

	private static final ConcurrentMap<String, String> tableNameMap = CollectionUtil.newConcurrentMap();
	private static final ConcurrentMap<String, String> keyPrefixMap = CollectionUtil.newConcurrentMap();

	/**
	 * 注册存储配置
	 * 
	 * @param sc
	 */
	public synchronized static void registStorageConfig(StorageConfig[] scs) {
		for (StorageConfig sc : scs) {
			String key = sc.getConfigKey();
			if (StringUtil.isEmpty(key)) {
				throw new KVException("storage config key is empty");
			}
			if (scMap.containsKey(key)) {
				throw new KVException("repeat key " + key);
			}
			if(!sc.enablePersistence() && !sc.enableCache()) {
				throw new KVException("where do you want to save data for " + key);
			}
			if (sc.enablePersistence()) {
				if (StringUtil.isEmpty(sc.getPersistentName())) {
					throw new KVException("persistent name is empty in " + key);
				}
				if (StringUtil.isEmpty(sc.getTableName())) {
					throw new KVException("table name is empty in " + key);
				}
				if(sc.isDailyExpired()) {
					throw new KVException("daily expired not support persistence for " + key);
				}
				if (StringUtil.isEmpty(sc.getKvPersistentName())) {
                    throw new KVException("key-value persistent name is empty in " + key);
                }
			}
			if (StringUtil.isEmpty(sc.getKeyPrefix())) {
				throw new KVException("key prefix is empty in " + key);
			}
			if (sc.enableCache()) {
				if (StringUtil.isEmpty(sc.getCacheGroupName())) {
					throw new KVException("cache group name is empty in " + key);
				}
				if (sc.getCacheExpireTime() >= HeConsts.SECONDS_PER_DAY * 30) {
					throw new KVException("error cache expire time in " + key);
				}
                if (sc.getLiveTime() >= HeConsts.SECONDS_PER_DAY * 30) {
                    throw new KVException("error cmem cache live time in " + key);
                }
				if (sc.isDailyExpired()) {
					TwoTuple<Integer, Integer> t = sc.getDailyExpiredTime();
					if (t == null) {
						throw new KVException("miss daily expire time in " + key);
					}
					if (t.first < 0 || t.first > 23 || t.second < 0 || t.second > 59) {
						throw new KVException("daily expire time error in " + key);
					}
				}
			}
			if (!StringUtil.isEmpty(sc.getTableName())) {
			    String tableKey = sc.getPersistentName() + "_" + sc.getTableName(); 
				String old = tableNameMap.putIfAbsent(tableKey, key);
				if (old != null && !key.equals(old)) {
					throw new KVException("repeat tableName " + sc.getTableName() + " in key " + key + " and " + old);
				}
			}
			if (!StringUtil.isEmpty(sc.getKeyPrefix())) {
                String keyPrefix = sc.getKvPersistentName() + "_" + sc.getKeyPrefix(); 
				String old = keyPrefixMap.putIfAbsent(keyPrefix, key);
				if (old != null && !key.equals(old)) {
					throw new KVException("repeat keyPrefix " + sc.getKeyPrefix() + " in key " + key + " and " + old);
				}
			}
			scMap.put(key, sc);
		}
	}

	/**
	 * 根据配置的key获取存储配置
	 * @param key
	 */
	public static StorageConfig getStorageConfig(String key) {
		return scMap.get(key);
	}

	/**
	 * 得到dao的实现类
	 * 
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getDAO(Class<T> cls) {
		T proxy = (T) proxyMap.get(cls);
		if (proxy == null) {
			synchronized (cls) {
				proxy = (T) proxyMap.get(cls);
				if (proxy == null) {
					Enhancer e = new Enhancer();
					e.setSuperclass(cls);
					e.setCallback(new StorageProxy(cls));
					proxy = (T) e.create();
					proxyMap.put(cls, proxy);
				}
			}
		}
		return proxy;
	}

}
