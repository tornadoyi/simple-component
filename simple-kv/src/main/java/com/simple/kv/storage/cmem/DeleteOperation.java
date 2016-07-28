package com.simple.kv.storage.cmem;

import java.lang.reflect.Method;

import com.simple.kv.storage.AbstractOperation;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.cmem.client.KVStorageClient;
import com.simple.kv.storage.cmem.client.KVStorageClientFactory;

class DeleteOperation extends AbstractOperation {

	private KVStorageClient client;
	
	public DeleteOperation(StorageInfo info, Method method) {
		super(info, method);
        
        client = KVStorageClientFactory.getStorageClient(info.getStorageConfig().getKvPersistentName());
	}

	@Override
	public Object execute(Object[] args) {
		Object keyParam = getKeyParam4Single(args);
		String realKey = buildRealKey(keyParam);
		return client.delete(realKey);
	}

}
