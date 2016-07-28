package com.simple.kv.storage.cmem;

import java.lang.reflect.Method;

import com.simple.kv.storage.IOperation;
import com.simple.kv.storage.OperationFactory;
import com.simple.kv.storage.OperationType;
import com.simple.kv.storage.StorageInfo;
import com.simple.kv.storage.error.KVException;

/**
 * 使用cmem做存储引擎时的操作对象工厂类
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public class CmemOperationFactory implements OperationFactory {

	@Override
	public IOperation getOperation(StorageInfo info, Method method) {
		return getOperation(info.getType(), info, method);
	}

	@Override
	public IOperation getOperation(OperationType type, StorageInfo info, Method method) {
		switch (type) {
		case GET:
			return new GetOperation(info, method);
		case SET:
			return new SetOperation(info, method);
		case ADD:
			return new AddOperation(info, method);
		case DELETE:
			return new DeleteOperation(info, method);
		case BATCH_SET:
			return new BatchSetOperation(info, method);
		case BATCH_GET:
			return new BatchGetOperation(info, method);
		case BATCH_DELETE:
			return new BatchDeleteOperation(info, method);
		case GET_FOR_CAS:
			return new GetForCasOperation(info, method);
		case CAS:
			return new CasOperation(info, method);
		default:
			throw new KVException("unsupport operation" + info.getType());
		}
	}

}
