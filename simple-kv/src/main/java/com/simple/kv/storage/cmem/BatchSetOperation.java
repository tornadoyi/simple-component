package com.simple.kv.storage.cmem;

import java.lang.reflect.Method;

import com.simple.base.util.tuple.TwoTuple;
import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.AbstractBatchOperation;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.cmem.client.KVStorageClient;
import com.simple.kv.storage.cmem.client.KVStorageClientFactory;

class BatchSetOperation extends AbstractBatchOperation {

	private KVStorageClient client;
	
	public BatchSetOperation(StorageInfo info, Method method) {
		super(info, method);
        
        client = KVStorageClientFactory.getStorageClient(info.getStorageConfig().getKvPersistentName());
	}

	@Override
	public Object execute(Object[] args) {
		TwoTuple<Integer, String[]> keyParamInfo = info.getKeyParamInfo();
		Object keyParam = args[keyParamInfo.first];
		final int liveTime = info.getStorageConfig().getLiveTime();
		int vi = info.getValueParamIndex();
		Object valueParam = args[vi];
		final TypeInfo valueItemType = getItemType4Batch(paramTypes[vi]);
		traversal(keyParam, valueParam, new ItemHandler() {
			@Override
			public void handle(Object keyItem, Object valueItem) {
				if (valueItem == null) {
					throw new IllegalArgumentException("cmem value can not be null");
				}
				String realKey = buildRealKey(keyItem);
				byte[] bytes = encode(keyItem, valueItem, valueItemType);
				client.set(realKey, bytes, liveTime);
			}
		});
		return null;
	}

}
