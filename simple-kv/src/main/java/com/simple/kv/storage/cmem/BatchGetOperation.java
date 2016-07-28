package com.simple.kv.storage.cmem;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import com.simple.base.util.CollectionUtil;
import com.simple.base.util.tuple.TwoTuple;
import com.simple.kv.storage.AbstractBatchOperation;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.cmem.client.KVStorageClient;
import com.simple.kv.storage.cmem.client.KVStorageClientFactory;

class BatchGetOperation extends AbstractBatchOperation {

	private KVStorageClient client;
	
	public BatchGetOperation(StorageInfo info, Method method) {
		super(info, method);
        
        client = KVStorageClientFactory.getStorageClient(info.getStorageConfig().getKvPersistentName());
	}

	@Override
	public Object execute(Object[] args) {
		TwoTuple<Integer, String[]> keyParamInfo = info.getKeyParamInfo();
		Object keyParam = args[keyParamInfo.first];
		final Map<String, Object> keyMap = CollectionUtil.newLinkedHashMap(); // 尽量保证结果顺序与key的顺序相同, 因此用LinkedHashMap
		traversal(keyParam, null, new ItemHandler() {
			@Override
			public void handle(Object keyItem, Object valueItem) {
				String realKey = buildRealKey(keyItem);
				keyMap.put(realKey, keyItem);
			}
		});
		Map<String, Object> tmpResult = client.getBulk(keyMap.keySet());
		Map<Object, Object> resultMap = CollectionUtil.newHashMap();
		if(!CollectionUtil.isEmpty(tmpResult)) {
			for (Map.Entry<String, Object> entry : tmpResult.entrySet()) {
				String realKey = entry.getKey();
				Object keyItem = keyMap.get(realKey);
				Object value = entry.getValue();
				if(value != null) {
					value = this.parseResult(keyItem, (byte[]) value);
					resultMap.put(keyItem, value);
				}
			}
		}

		if (returnType.isMap()) {
			return resultMap;
		} else {
			Collection<Object> resultCol = CollectionUtil.newArrayList();
			for (Object keyItem : keyMap.values()) {
				Object valueItem = resultMap.get(keyItem);
				if (valueItem != null) {
					resultCol.add(valueItem);
				}
			}
			return returnType.isCollection() ? resultCol : resultCol.toArray((Object[]) Array.newInstance(returnType.getArrayType().getRawClass(), resultCol.size()));
		}
	}
}
