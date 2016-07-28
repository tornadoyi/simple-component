package com.simple.kv.storage.cmem;

import java.lang.reflect.Method;

import com.simple.kv.storage.AbstractOperation;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.cmem.client.KVStorageClient;
import com.simple.kv.storage.cmem.client.KVStorageClientFactory;
import com.simple.kv.storage.error.KVException;

class GetOperation extends AbstractOperation {

	private KVStorageClient client;
	
	public GetOperation(StorageInfo info, Method method) {
		super(info, method);
		
		client = KVStorageClientFactory.getStorageClient(info.getStorageConfig().getKvPersistentName());
	}

	@Override
	public Object execute(Object[] args) {
		Object keyParam = getKeyParam4Single(args);
		String realKey = buildRealKey(keyParam);
		byte[] bytes;
		try {
			bytes = (byte[])client.get(realKey);
		} catch (RuntimeException e) {
			throw new KVException("CMEM_OPERATION_ERROR", e);
		}
		Object result = null;
		if (bytes != null) {
			result = this.parseResult(keyParam, bytes);
		}
		return result;
	}

}
