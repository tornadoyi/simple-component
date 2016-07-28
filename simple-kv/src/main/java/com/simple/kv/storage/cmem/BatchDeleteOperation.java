package com.simple.kv.storage.cmem;

import java.lang.reflect.Method;

import com.simple.base.util.tuple.TwoTuple;
import com.simple.kv.storage.AbstractBatchOperation;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.cmem.client.KVStorageClient;
import com.simple.kv.storage.cmem.client.KVStorageClientFactory;

class BatchDeleteOperation extends AbstractBatchOperation {
	
	private KVStorageClient client;

	public BatchDeleteOperation(StorageInfo info, Method method) {
		super(info, method);
        
        client = KVStorageClientFactory.getStorageClient(info.getStorageConfig().getKvPersistentName());
	}

	@Override
	public Object execute(Object[] args) {
		TwoTuple<Integer, String[]> keyParamInfo = info.getKeyParamInfo();
		Object keyParam = args[keyParamInfo.first];
		traversal(keyParam, null, new ItemHandler() {
			@Override
			public void handle(Object keyItem, Object valueItem) {
				String realKey = buildRealKey(keyItem);
				client.delete(realKey);
			}
		});
		return null;
	}

}
