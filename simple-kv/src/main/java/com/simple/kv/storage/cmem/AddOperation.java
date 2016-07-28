package com.simple.kv.storage.cmem;

import java.lang.reflect.Method;

import com.simple.kv.storage.AbstractOperation;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.cmem.client.KVStorageClient;
import com.simple.kv.storage.cmem.client.KVStorageClientFactory;

class AddOperation extends AbstractOperation {
	
	private KVStorageClient client;

	public AddOperation(StorageInfo info, Method method) {
		super(info, method);
        
        client = KVStorageClientFactory.getStorageClient(info.getStorageConfig().getKvPersistentName());
	}

	@Override
	public Object execute(Object[] args) {
		Object keyParam = getKeyParam4Single(args);
		String realKey = buildRealKey(keyParam);
		Object value = args[info.getValueParamIndex()];
		if (value == null) {
			throw new IllegalArgumentException("cmem value can not be null");
		}
		byte[] bytes = this.encode(keyParam, value, paramTypes[info.getValueParamIndex()]);
		final int liveTime = info.getStorageConfig().getLiveTime();
		return client.add(realKey, bytes, liveTime);
	}

}
