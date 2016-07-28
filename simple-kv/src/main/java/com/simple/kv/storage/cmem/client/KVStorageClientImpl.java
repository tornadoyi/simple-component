/**
 * created by haitao.yao @ May 10, 2011
 */
package com.simple.kv.storage.cmem.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.storage.cmem.client.DataModifyListener.Operation;
import com.simple.kv.storage.cmem.client.KVStorageDescriptor.Server;
import com.simple.kv.storage.cmem.client.memcached.MemcachedClient;
import com.simple.kv.storage.cmem.client.memcached.SockIOPool;

/**
 * @author haitao.yao @ May 10, 2011
 * 
 */
public class KVStorageClientImpl implements KVStorageClient {

	/**
	 * the max size for mget
	 */
	public static final int MAX_MGET_SIZE = 255;

	private static final Logger logger = Logger
			.getLogger(KVStorageClientImpl.class);

	private LBMemcachedClient client;

	private DataModifyListener dataListener = null;

	private String currentPoolName = null;

	private static final AtomicInteger POOL_INDEX = new AtomicInteger(1);

	private static final String POOL_NAME_FORMAT = "yyyy-MM-dd.HH:mm:ss";

//	private static final KVStorageClient instance = new KVStorageClientImpl();

	private String[] oldServerList;

	private String oldPoolName;
	
	private static Map<String, KVStorageClientImpl> kvsClients;
	
	static {
	
		init();
	}

	protected KVStorageClientImpl() {
		
	}

	public static final KVStorageClient getInstance(String name) {
	    return kvsClients.get(name);
	}
	private static synchronized void init(){
		KVStorageConfig config = new KVStorageConfig();
		
		KVStorageDescriptor des1 = new KVStorageDescriptor();
		Server server = new Server();
		server.setAddress("10.130.137.64:11211");
		List<Server> serverList = CollectionUtil.newArrayList();
		serverList.add(server);
		des1.setServers(serverList);
		des1.setName("default");
		des1.after();
		config.getKvsDescriptors().add(des1);
		config.after();
		
		
		
		Map<String, KVStorageClientImpl> tempClients = new HashMap<String, KVStorageClientImpl>();
		for (KVStorageDescriptor kvStorageDescriptor : config.getKvsDescriptors()){
			KVStorageClientImpl impl = new KVStorageClientImpl();
			impl.init(kvStorageDescriptor);
			tempClients.put(kvStorageDescriptor.getName(), impl);
		}
		kvsClients = tempClients;
	}

	private void init(KVStorageDescriptor kvStorageDescriptor) {
		logger.info("KVStorage initing...");
		if (currentPoolName != null) {
			oldPoolName = currentPoolName;
			logger.warn("old pool name: " + oldPoolName);
		}
		currentPoolName = createPoolName();
		if (kvStorageDescriptor == null) {
			throw new IllegalStateException("config error for kv storage");
		}
		this.client = new LBMemcachedClient(createClients(kvStorageDescriptor));
		logger.info("current kv storage pool name: " + this.currentPoolName);
		stopOldPools();
		this.oldServerList = kvStorageDescriptor.getServersArray();
		logger.info("KVStorage init done");
	}

	private void stopOldPools() {
		if (oldPoolName != null && oldServerList != null) {
			try {
				Thread.sleep(getMaxTimeout());
			} catch (InterruptedException e) {
			}
			// shutdown the old pool
			for (String oldServer : oldServerList) {
				SockIOPool oldPool = SockIOPool.getInstance(this
						.getPoolNameForServer(oldPoolName, oldServer));
				if (oldPool != null) {
					oldPool.shutDown();
				} else {
					logger.error("no pool named : "
							+ this.getPoolNameForServer(oldPoolName, oldServer));
				}
			}
			logger.info("old pool shut down");
		}
	}

	private String getPoolNameForServer(String poolNamePrefix,
			String serverAddress) {
		return poolNamePrefix + serverAddress;
	}

	private List<MemcachedClient> createClients(KVStorageDescriptor config) {
		String[] serverArray = config.getServersArray();
		List<MemcachedClient> clients = new ArrayList<MemcachedClient>(
				serverArray.length);
		for (String server : serverArray) {
			String poolNameForServer = this.getPoolNameForServer(
					currentPoolName, server);
			SockIOPool pool = SockIOPool.getInstance(poolNameForServer);
			pool.setServers(new String[] { server });
			if (config.getInitConnection() > 0) {
				pool.setInitConn(config.getInitConnection());
			}
			if (config.getMaxConnection() > 0) {
				pool.setMaxConn(config.getMaxConnection());
			}
			if (config.getMaxIdle() > 0) {
				pool.setMaxIdle(config.getMaxIdle());
			}
			if (config.getMinConnection() > 0) {
				pool.setMinConn(config.getMinConnection());
			}
			if (config.getSocketTimeout() > 0) {
				pool.setSocketTO(config.getSocketTimeout());
			}
			if (config.getSocketConnectTimeout() > 0) {
				pool.setSocketConnectTO(config.getSocketConnectTimeout());
			}
			if (config.getMaxRetryDelay() > 0) {
				pool.setConfiguredMaxRetryDelay(config.getMaxRetryDelay());
			}
			pool.initialize();
			if (config.isNeedErrorHandler()){
				clients.add(new MemcachedClient(null, new ErrorHandlerImpl(), poolNameForServer));
			} else {
				clients.add(new MemcachedClient(poolNameForServer));
			}
		}
		return clients;
	}

	private long getMaxTimeout() {
		return 5 * 1000;
	}
	//just return a pool name wiht increment number and the time
	private static final String createPoolName() {
		return "SockIOPool-" + POOL_INDEX.getAndIncrement() + "-@"
				+ new SimpleDateFormat(POOL_NAME_FORMAT).format(new Date());
	}

	/**
	 * transform the seconds to the date
	 * will check the value is invalid
	 * @param expire
	 * @return
	 */
	private Date toDate(long expire){
		if(expire == KVStorageClient.EXPIRE_NEVER){
			return new Date(0);
		}
		if(expire < 0 || expire > KVStorageClient.EXPIRE_MAX){
			throw new IllegalArgumentException("expire is invalid: " + expire);
		}
		return new Date(expire * 1000L);
	}
	
	public boolean delete(String key) {
		nodifyDataChange(key, Operation.DELETE);
		return client.delete(key);
	}

	public boolean set(String key, Object value) {
		nodifyDataChange(key, Operation.SET);
		boolean result = client.set(key, value);
		return result;
	}

	@Override
	public boolean set(String key, Object value, int expire) {
		nodifyDataChange(key, Operation.SET);
		boolean result =  client.set(key, value, toDate(expire));
		return result;
	}

	public boolean add(String key, Object value) {
		nodifyDataChange(key, Operation.ADD);
		boolean result = client.add(key, value);
		return result;
	}
	
	@Override
	public boolean add(String key, Object value, int expire) {
		nodifyDataChange(key, Operation.ADD);
		boolean result = client.add(key, value, toDate(expire)); 
		return  result;
	}

	public boolean replace(String key, Object value) {
		nodifyDataChange(key, Operation.REPLACE);
		boolean result = client.replace(key, value);
		return result;
	}

	public long incr(String key) {
		nodifyDataChange(key, Operation.INCR);
		return client.incr(key);
	}

	public long incr(String key, int inc) {
		nodifyDataChange(key, Operation.INCR);
		return client.incr(key, inc);
	}

	public long decr(String key) {
		nodifyDataChange(key, Operation.DECR);
		return client.decr(key);
	}

	public long decr(String key, int inc) {
		nodifyDataChange(key, Operation.DECR);
		return client.decr(key, inc);
	}

	public Object get(String key) {
		return client.get(key);
	}
	
	@Override
	public Object getExt(String key) {
		return client.getExt(key);
	}

	public Collection<?> getBulkCollection(Collection<String> keys) {
		if (keys == null || keys.isEmpty()) {
			return null;
		}
		List<Object> result = null;
		if (keys.size() < MAX_MGET_SIZE) {
			Object[] array = client.getMultiArray(keys.toArray(new String[keys.size()]));
			if (array != null) {
				result = Arrays.asList(array);
			}
		} else {
			Collection<String[]> subKeys = cutKeys(keys);
			result = new ArrayList<Object>(keys.size());
			for (String[] keyArray : subKeys) {
				Object[] array = client.getMultiArray(keyArray);
				if (array != null) {
					result.addAll(Arrays.asList(array));
				}
			}
		}
		return result;
	}

	protected static final Collection<String[]> cutKeys(Collection<String> keys) {
		List<String[]> result = new ArrayList<String[]>(keys.size() / MAX_MGET_SIZE + 1);
		String[] keysArray = keys.toArray(new String[keys.size()]);
		int start = 0;
		int end = MAX_MGET_SIZE;
		while (start < keysArray.length) {
			end = end > keysArray.length ? keysArray.length : end;
			int size = end - start;
			String[] subKeys = new String[size];
			System.arraycopy(keysArray, start, subKeys, 0, size);
			result.add(subKeys);
			start += MAX_MGET_SIZE;
			end += MAX_MGET_SIZE;
		}
		return result;
	}

	public Map<String, Object> getBulk(Collection<String> keys) {
		if (keys == null || keys.isEmpty()) {
			return null;
		}
		if (keys.size() < MAX_MGET_SIZE) {
			return client.getMulti(keys.toArray(new String[keys.size()]));
		} else {
			Map<String, Object> result = new HashMap<String, Object>((int) (keys.size() * 1.5));
			for (String[] keyArray : cutKeys(keys)) {
				Map<String, Object> subResult = client.getMulti(keyArray);
				result.putAll(subResult);
			}
			return result;
		}
	}

	public boolean storeCounter(String key, long value) {
		this.nodifyDataChange(key, Operation.STORE_COUNTER);
		return client.storeCounter(key, value);
	}

	public CASValue gets(String key) {
		return this.client.gets(key);
	}

	@Override
	public CASValue getsExt(String key) {
		return this.client.getsExt(key);
	}
	
	public CASResponse cas(String key, long casId, Object value) {
		this.nodifyDataChange(key, Operation.CAS);
		return this.client.cas(key, casId, value);
	}
	
	@Override
	public CASResponse cas(String key, long casId, Object value, int expire) {
		this.nodifyDataChange(key, Operation.CAS);
		return client.cas(key, casId, value, toDate(expire));
	}
	

	public void nodifyDataChange(String key, DataModifyListener.Operation operation) {
		if (this.dataListener != null) {
			try {
				this.dataListener.dataModified(key, operation);
			} catch (Throwable e) {
				logger.error("failed to notify data change, key: " + key
						+ ", operation: " + operation);
			}
		}
	}

	public void registerDataListener(DataModifyListener listener) {
		if (listener != null) {
			this.dataListener = listener;
		}
	}

	@Override
	public long decr(String key, int inc, long def) {
		long returnValue = this.decr(key, inc);
		long result = returnValue;
		if (returnValue == -1) {
			if (this.storeCounter(key, def)) {
				result = def;
			} else {
				result = this.decr(key, inc);
			}
		}
		return result;
	}

	@Override
	public long incr(String key, int inc, long def) {
		long returnValue = this.incr(key, inc);
		long result = returnValue;
		if (returnValue == -1) {
			if (this.client.storeCounter(key, def)) {
				result = def;
			} else {
				result = this.incr(key, inc);
			}
		}
		this.nodifyDataChange(key, Operation.INCR);
		return result;
	}

	@Override
	public long getCounter(String key) {
		return this.client.getCounter(key);
	}
	
	@Override
	public Map<String, Object> getBulkExt(Collection<String> keys) {
		if (keys == null || keys.isEmpty()) {
			return null;
		}
		if (keys.size() < MAX_MGET_SIZE) {
			return client.getMultiExt(keys.toArray(new String[keys.size()]));
		} else {
			Map<String, Object> result = new HashMap<String, Object>((int) (keys.size() * 1.5));
			for (String[] keyArray : cutKeys(keys)) {
				Map<String, Object> subResult = client.getMultiExt(keyArray);
				result.putAll(subResult);
			}
			return result;
		}
	}

	private static class LBMemcachedClient implements IMemcachedClient {

		private final List<MemcachedClient> innerClients;

		private final int clientCount;

		public LBMemcachedClient(List<MemcachedClient> innerClients) {
			super();
			if (innerClients == null || innerClients.isEmpty()) {
				throw new IllegalArgumentException(
						"innerClients should not be null or empty");
			}
			this.innerClients = innerClients;
			this.clientCount = innerClients.size();
		}

		private MemcachedClient getClient(String key) {
			int value = key.hashCode() % this.clientCount;
			return innerClients.get(value < 0 ? -value : value);
		}

		public boolean delete(String key) {
			return getClient(key).delete(key);
		}

		public boolean delete(String key, Date expiry) {
			return getClient(key).delete(key, expiry);
		}

		public boolean set(String key, Object value) {
			return getClient(key).set(key, value);
		}

		public boolean add(String key, Object value) {
			return getClient(key).add(key, value);
		}

		public boolean add(String key, Object value, Integer hashCode) {
			return getClient(key).add(key, value, hashCode);
		}

		public boolean replace(String key, Object value) {
			return getClient(key).replace(key, value);
		}

		public boolean replace(String key, Object value, Integer hashCode) {
			return getClient(key).replace(key, value, hashCode);
		}

		public boolean replace(String key, Object value, Date expiry) {
			return getClient(key).replace(key, value, expiry);
		}

		public boolean replace(String key, Object value, Date expiry,
				Integer hashCode) {
			return getClient(key).replace(key, value, expiry, hashCode);
		}

		public boolean storeCounter(String key, long counter) {
			return getClient(key).storeCounter(key, counter);
		}

		public boolean storeCounter(String key, Long counter) {
			return getClient(key).storeCounter(key, counter);
		}

		public boolean storeCounter(String key, Long counter, Integer hashCode) {
			return getClient(key).storeCounter(key, counter, hashCode);
		}

		public long getCounter(String key) {
			return getClient(key).getCounter(key);
		}

		public long getCounter(String key, Integer hashCode) {
			return getClient(key).getCounter(key, hashCode);
		}

		public long addOrIncr(String key) {
			return getClient(key).addOrIncr(key);
		}

		public long addOrIncr(String key, long inc) {
			return getClient(key).addOrIncr(key, inc);
		}

		public long addOrIncr(String key, long inc, Integer hashCode) {
			return getClient(key).addOrIncr(key, inc, hashCode);
		}

		public long addOrDecr(String key) {
			return getClient(key).addOrDecr(key);
		}

		public long addOrDecr(String key, long inc) {
			return getClient(key).addOrDecr(key, inc);
		}

		public long addOrDecr(String key, long inc, Integer hashCode) {
			return getClient(key).addOrDecr(key, inc, hashCode);
		}

		public long incr(String key) {
			return getClient(key).incr(key);
		}

		public long incr(String key, long inc) {
			return getClient(key).incr(key, inc);
		}

		public long incr(String key, long inc, Integer hashCode) {
			return getClient(key).incr(key, inc, hashCode);
		}

		public long decr(String key) {
			return getClient(key).decr(key);
		}

		public long decr(String key, long inc) {
			return getClient(key).decr(key, inc);
		}

		public long decr(String key, long inc, Integer hashCode) {
			return getClient(key).decr(key, inc, hashCode);
		}

		public Object get(String key) {
			return getClient(key).get(key);
		}

		public Object get(String key, Integer hashCode) {
			return getClient(key).get(key, hashCode);
		}

		public Object get(String key, Integer hashCode, boolean asString) {
			return getClient(key).get(key, hashCode, asString);
		}
		
		public Object getExt(String key){
			return getClient(key).getExt(key);
		}
		
		public Object[] getMultiArray(String[] keys) {
			Map<String, Object> rawResult = this.getMulti(keys);
			Object[] result = new Object[keys.length];
			int i = 0;
			for (String key : keys) {
				result[i++] = rawResult.get(key);
			}
			return result;
		}

		public Map<String, Object> getMulti(String[] keys) {
			Map<MemcachedClient, List<String>> clients = new HashMap<MemcachedClient, List<String>>();
			for (String key : keys) {
				MemcachedClient c = getClient(key);
				List<String> container = clients.get(c);
				if (container == null) {
					container = new ArrayList<String>(keys.length);
					clients.put(c, container);
				}
				container.add(key);
			}
			Map<String, Object> result = new HashMap<String, Object>(
					(int) (keys.length * 1.5));
			for (Entry<MemcachedClient, List<String>> entry : clients
					.entrySet()) {
				result.putAll(entry.getKey().getMulti(
						entry.getValue().toArray(
								new String[entry.getValue().size()])));
			}
			return result;
		}

		
		public Map<String, Object> getMultiExt(String[] keys) {
			Map<MemcachedClient, List<String>> clients = new HashMap<MemcachedClient, List<String>>();
			for (String key : keys) {
				MemcachedClient c = getClient(key);
				List<String> container = clients.get(c);
				if (container == null) {
					container = new ArrayList<String>(keys.length);
					clients.put(c, container);
				}
				container.add(key);
			}
			Map<String, Object> result = new HashMap<String, Object>(
					(int) (keys.length * 1.5));
			for (Entry<MemcachedClient, List<String>> entry : clients
					.entrySet()) {
				result.putAll(entry.getKey().getMultiExt(
						entry.getValue().toArray(
								new String[entry.getValue().size()])));
			}
			return result;
		}
		
		
		public CASValue gets(String key) {
			return getClient(key).gets(key);
		}
		
		public CASValue getsExt(String key) {
			return getClient(key).getsExt(key);
		}

		public CASResponse cas(String key, long casId, Object value) {
			return getClient(key).cas(key, casId, value);
		}

		@Override
		public boolean set(String key, Object value, Date expiry) {
			return getClient(key).set(key, value, expiry);
		}

		@Override
		public boolean set(String key, Object value, Date expiry, Integer hashCode) {
			return getClient(key).set(key, value, expiry, hashCode);
		}

		@Override
		public boolean add(String key, Object value, Date expiry) {
			return getClient(key).add(key, value, expiry);
		}

		@Override
		public boolean add(String key, Object value, Date expiry, Integer hashCode) {
			return getClient(key).add(key, value, expiry, hashCode);
		}

		@Override
		public CASResponse cas(String key, long casId, Object value, Date expire) {
			return getClient(key).cas(key, casId, value,expire);
		}
	}
}
