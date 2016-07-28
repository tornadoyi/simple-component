package com.simple.kv.storage.cmem;

import java.lang.reflect.Method;

import com.simple.kv.storage.AbstractOperation;
import com.simple.kv.storage.CasResult;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.cmem.client.CASValue;
import com.simple.kv.storage.cmem.client.KVStorageClient;
import com.simple.kv.storage.cmem.client.KVStorageClientFactory;
import com.simple.kv.storage.error.KVException;

class GetForCasOperation extends AbstractOperation {
	
	private KVStorageClient client;

	public GetForCasOperation(StorageInfo info, Method method) {
		super(info, method);
		resultItemType = returnType.getGenericTypes().get(0);
        
        client = KVStorageClientFactory.getStorageClient(info.getStorageConfig().getKvPersistentName());
	}

	@Override
	public Object execute(Object[] args) {
		Object keyParam = getKeyParam4Single(args);
		String realKey = buildRealKey(keyParam);
		CASValue cv;
		try {
			cv = client.gets(realKey);
		} catch (RuntimeException e) {
			throw new KVException(e.getMessage());
		}
		if (cv != null) {
			Object value = cv.getValue();
			value = this.parseResult(keyParam, (byte[]) value);
			return new CasResult<Object>(cv.getCas(), value);
		}
		return CasResult.NOT_FOUND_RESULT;
	}

}
