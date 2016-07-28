package com.simple.kv.storage;

import com.simple.kv.storage.cmem.CmemOperationFactory;


/**
 * 持久化的模式，目前支持db,cmem
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * @author <a href="mailto:richie.yan@happyelements.com">richie.yan</a>
 *
 */
public enum PersistentMode {
//TODO 暂时先不实现DB
//	DB {
//		@Override
//		OperationFactory getOperationFactory() {
//			return OP_FACTORY_DB;
//		}
//		@Override
//		boolean supportCache() {
//			return true;
//		}
//		
//	},
	CMEM {
		@Override
		OperationFactory getOperationFactory() {
			return OP_FACTORY_CMEM;
		}
		@Override
		boolean supportCache() {
			return false;
		}
	},
	;
	
	//private static final OperationFactory OP_FACTORY_DB = new DbOperationFactory();
	private static final OperationFactory OP_FACTORY_CMEM = new CmemOperationFactory();

    abstract OperationFactory getOperationFactory();
	
	abstract boolean supportCache();
	
	
}
