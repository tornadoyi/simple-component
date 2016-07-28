package com.simple.kv.storage.cmem;

import java.lang.reflect.Method;

import com.simple.kv.storage.AbstractOperation;
import com.simple.kv.storage.CasResult;
import com.simple.kv.storage.CasStatus;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.cmem.client.CASResponse;
import com.simple.kv.storage.cmem.client.KVStorageClient;
import com.simple.kv.storage.cmem.client.KVStorageClientFactory;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.error.KVException;

class CasOperation extends AbstractOperation {

	private KVStorageClient client;

	private final AddOperation addOperation;

	public CasOperation(StorageInfo info, Method method) {
		super(info, method);
		addOperation = new AddOperation(info, method);
		
        client = KVStorageClientFactory.getStorageClient(info.getStorageConfig().getKvPersistentName());
	}

	@Override
	public Object execute(Object[] args) {
		final Long version = ConverterType.TYPE_LONG.convert(args[info.getCasVersionIndex()]);
		if (version == CasResult.NOT_FOUND_VERSION) {
			boolean result = (Boolean) addOperation.execute(args);
			return result ? CasStatus.OK : CasStatus.VERSION_INVALID;
		}
		Object keyParam = getKeyParam4Single(args);
		String realKey = buildRealKey(keyParam);
		final int liveTime = info.getStorageConfig().getLiveTime();
        Object value = args[info.getValueParamIndex()];
		if (value == null) {
			throw new IllegalArgumentException("cmem value can not be null");
		}
		CASResponse cr = client.cas(realKey, version, this.encode(keyParam, value, paramTypes[info.getValueParamIndex()]), liveTime);
		if (cr == null || cr == CASResponse.ERROR) {
			throw new KVException("CMEM_OPERATION_ERROR cas response is " + cr);
		}
		return cr == CASResponse.OK ? CasStatus.OK : (cr == CASResponse.EXISTS ? CasStatus.VERSION_INVALID : CasStatus.NOT_FOUND);
	}

}
