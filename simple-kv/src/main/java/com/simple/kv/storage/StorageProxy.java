package com.simple.kv.storage;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.simple.base.util.CollectionUtil;
import com.simple.base.util.tuple.Tuple;
import com.simple.kv.storage.anno.CasVersion;
import com.simple.kv.storage.anno.PValue;
import com.simple.kv.storage.anno.Pkey;
import com.simple.kv.storage.anno.Storage;
import com.simple.kv.storage.error.KVException;

/**
 * 存储的代理类
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
class StorageProxy implements MethodInterceptor {

	private final Map<Method, IOperation> operationMap = CollectionUtil.newConcurrentMap();

	private final Class<?> originalCls;

	StorageProxy(Class<?> cls) {
		originalCls = cls;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		IOperation operation = operationMap.get(method);
		if (operation == null) {
			synchronized (method) {
				operation = operationMap.get(method);
				if (operation == null) {
					operation = parseOperation(method);
					operationMap.put(method, operation);
				}
			}
		}
		if (operation == OperationFactory.NONE_OPERATION) { // 自定义实现
			return proxy.invokeSuper(obj, args);
		}
		try {
			return operation.execute(args);
		} finally { // 清除本次调用的上下文环境
			StorageContext.clear();
		}
	}

	private IOperation parseOperation(Method method) {
		StorageInfo info;
		Storage pt = method.getAnnotation(Storage.class);
		if (pt != null) {
			info = new StorageInfo();
			info.type = pt.type();
			String scKey = pt.storageConfigKey();
			StorageConfig sc = HeStorageFactory.getStorageConfig(scKey);
			if(sc == null) {
				throw new KVException("no such storage config for " + scKey);
			}
			info.sc = sc;
			Annotation[][] annos = method.getParameterAnnotations();
			int paramIndex = 0;
			for (Annotation[] paramAnnos : annos) {
				for (Annotation anno : paramAnnos) {
					if (anno instanceof Pkey) {
						if (info.keyParamInfo == null) {
							Pkey pkey = (Pkey) anno;
							info.keyParamInfo = Tuple.tuple(paramIndex, pkey.value());
						} else {
							throw new KVException("repeat key param in " + originalCls.getName() + " "
									+ method.getName());
						}
					}
					if (anno instanceof PValue) {
						if (info.valueParamIndex == -1) {
							info.valueParamIndex = paramIndex;
						} else {
							throw new KVException("repeat value param in " + originalCls.getName() + " "
									+ method.getName());
						}
					}
					if (anno instanceof CasVersion) {
						if (info.casVersionIndex == -1) {
							info.casVersionIndex = paramIndex;
						} else {
							throw new KVException("repeat cas version param in " + originalCls.getName() + " "
									+ method.getName());
						}
					}
				}
				++paramIndex;
			}
			if (info.keyParamInfo == null) { // key必须存在
				throw new KVException("key declare not found in " + originalCls.getName() + " " + method.getName());
			}
			if (info.type.needValue() && info.valueParamIndex == -1) { // 有些操作必须有value
				throw new KVException("value declare not found in " + originalCls.getName() + " " + method.getName());
			}
			if (info.type == OperationType.CAS && info.casVersionIndex == -1) { // cas操作必须有version
				throw new KVException("cas version declare not found in " + originalCls.getName() + " "
						+ method.getName());
			}
		} else {
			info = null;
		}
		if (info != null) {
			OperationFactory opf = null;
			if(info.enablePersistence()) {
				PersistentMode persistentMode = HeStorageFactory.getCurrentPersistentMode(info.sc);
				opf = persistentMode.getOperationFactory();
//				if(info.enableCache() && persistentMode.supportCache()) {
//                    opf = new CacheOperationFactory(opf);
//				}
			} 
			
//			else if(info.enableCache()) {
//				opf = new CacheOperationFactory();
//			}
//			if(opf != null && LocalCacheOperationFactory.isLocalCacheUseable()) {
//				opf = new LocalCacheOperationFactory(opf);
//			}
			return opf != null ? opf.getOperation(info, method) : null;
		} else {
			return OperationFactory.NONE_OPERATION;
		}
	}

}
