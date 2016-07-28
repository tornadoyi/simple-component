package com.simple.kv.storage;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.iterators.ArrayIterator;

import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.error.KVException;

public abstract class AbstractBatchOperation extends AbstractOperation {

	protected static interface LengthGetter {
		public int length(Object param);
	}

	static final LengthGetter arrayLengthGetter = new LengthGetter() {
		@Override
		public int length(Object param) {
			return Array.getLength(param);
		}
	};

	static final LengthGetter collectionLengthGetter = new LengthGetter() {
		@Override
		public int length(Object param) {
			return ((Collection<?>) param).size();
		}
	};

	static final LengthGetter mapLengthGetter = new LengthGetter() {
		@Override
		public int length(Object param) {
			return ((Map<?, ?>) param).size();
		}
	};

	protected LengthGetter keyLengthGetter;
	protected LengthGetter valueLengthGetter;

	public AbstractBatchOperation(StorageInfo info, Method method) {
		super(info, method);
		if (returnType.getRawClass() != void.class) {
			resultItemType = getItemType4Batch(returnType);
		}
	}

	@Override
	protected void checkKeyParamType() {
		TypeInfo pt = paramTypes[info.getKeyParamInfo().first];
		TypeInfo keyType;
		if (pt.isArray()) {
			keyType = pt.getArrayType();
			keyLengthGetter = arrayLengthGetter;
		} else if (pt.isCollection() || pt.isMap()) {
			keyType = pt.getGenericTypes().get(0);
			keyLengthGetter = pt.isCollection() ? collectionLengthGetter : mapLengthGetter;
		} else {
			throw new KVException("STORAGE_METHOD_DECLARE_ERROR error keyParam type for batch operation " + pt);
		}
		checkKeyItemType(keyType);
	}

	@Override
	protected void checkValueParamType() {
		if (info.getType().needValue()) {
			TypeInfo t = paramTypes[info.getValueParamIndex()];
			if (t.isArray()) {
				valueLengthGetter = arrayLengthGetter;
			} else if (t.isCollection()) {
				valueLengthGetter = collectionLengthGetter;
			} else if (t.isMap()) {
				valueLengthGetter = mapLengthGetter;
			} else {
				throw new KVException("STORAGE_METHOD_DECLARE_ERROR error valueParam type for batch operation " + t);
			}
		}
	}

	@Override
	protected void checkReturnType() {
		switch (info.getType()) {
		case BATCH_GET:
			if (returnType.isCollection()) {
				TypeInfo gt = returnType.getGenericTypes().get(0);
				if (!KeyParamAware.class.isAssignableFrom(gt.getRawClass())) {
					throw new KVException("if return type of batch get is Collection, the item of Collection must be KeyParamAware, or use Map instead");
				}
			} else if (returnType.isArray()) {
				if (!KeyParamAware.class.isAssignableFrom(returnType.getArrayType().getRawClass())) {
					throw new KVException("if return type of batch get is array, the item of array must be KeyParamAware, or use Map instead");
				}
			} else if (!returnType.isMap()) {
				throw new KVException("error return type for batch get " + returnType);
			}
			break;
		default:
			if (returnType.getRawClass() != void.class) {
				throw new KVException("only allow void return type for " + info.getType());
			}
			break;
		}
	}

	protected static interface ItemHandler {
		public void handle(Object keyItem, Object valueItem);
	}

	private Iterator<?> getIterator(Object obj) {
		if (obj instanceof Collection<?>) {
			return ((Collection<?>) obj).iterator();
		} else if (obj instanceof Map<?, ?>) {
			return ((Map<?, ?>) obj).entrySet().iterator();
		} else {
			return new ArrayIterator(obj);
		}
	}

	@SuppressWarnings("unchecked")
	protected void traversal(Object keyParam, Object valueParam, ItemHandler handler) {
		boolean kvSame = keyParam == valueParam;
		Iterator<?> iteKey = getIterator(keyParam);
		Iterator<?> iteValue = null;
		if (valueParam != null && !kvSame) {
			iteValue = getIterator(valueParam);
			int keyLength = keyLengthGetter.length(keyParam);
			int valueLength = valueLengthGetter.length(valueParam);
			if (valueLength != keyLength) {
				throw new KVException("NO_MAPPING_KEY_AND_VALUE" + method.getDeclaringClass().getName() + "." + method.getName());
			}
		}
		while (iteKey.hasNext()) {
			Object keyItem = iteKey.next();
			Object valueItem = iteValue != null ? iteValue.next() : kvSame ? keyItem : null;
			if (keyItem instanceof Map.Entry<?, ?>) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) keyItem;
				keyItem = entry.getKey();
				if (kvSame) {
					valueItem = entry.getValue();
				}
			} else if (keyItem instanceof KeyParamAware) {
				keyItem = ((KeyParamAware<Object>) keyItem).readKeyParam();
			}
			if (!kvSame && valueItem instanceof Map.Entry<?, ?>) {
				Map.Entry<?, ?> entry = (Map.Entry<?, ?>) valueItem;
				valueItem = entry.getValue();
			}
			handler.handle(keyItem, valueItem);
		}
	}

	protected TypeInfo getItemType4Batch(TypeInfo paramType) {
		if (paramType.isCollection()) {
			return paramType.getGenericTypes().get(0);
		} else if (paramType.isMap()) {
			return paramType.getGenericTypes().get(1);
		} else if (paramType.isArray()) {
			return paramType.getArrayType();
		} else {
			throw new KVException("error type for batch operation " + paramType + " in method " + method);
		}
	}

	protected String buildBatchGetUnionSql(String prefixSql, int unionSize){
		String template = this.buildSingleGetDelSql(prefixSql);
		StringBuilder sql = new StringBuilder();
		for(int i = 0; i < unionSize; i++){
			sql.append(template);
			if(i != unionSize-1){
				sql.append(" union ");
			}
		}
		return sql.toString();
	}
	
	protected String buildSingleGetDelSql(String prefixSql){
		String[] fieldNames = info.getKeyParamInfo().second;
		StringBuilder baseSql = new StringBuilder(prefixSql).append(" from ").append(info.getTableName()).append(" where ");
		StringBuilder conditionSql = new StringBuilder("(");
		for (int i = 0; i < fieldNames.length; i++) {
			conditionSql.append("`").append(fieldNames[i]).append("`=?").append(i < fieldNames.length - 1 ? " and " : ")");
		}
		StringBuilder sql = new StringBuilder(baseSql);
		sql.append(conditionSql);
		sql.append(" limit ").append(1);
		return sql.toString();
	}
	
	protected String buildBatchGetDelSql(String prefixSql, int size) {
		String[] fieldNames = info.getKeyParamInfo().second;
		StringBuilder baseSql = new StringBuilder(prefixSql).append(" from ").append(info.getTableName()).append(" where ");
		StringBuilder conditionSql = new StringBuilder();
		if (fieldNames.length == 1) {
			baseSql.append("`").append(fieldNames[0]).append("` in ");
			conditionSql.append("?");
		} else {
			conditionSql.append("(");
			for (int i = 0; i < fieldNames.length; i++) {
				conditionSql.append("`").append(fieldNames[i]).append("`=?").append(i < fieldNames.length - 1 ? " and " : ")");
			}
		}
		StringBuilder sql = new StringBuilder(baseSql);
		if (fieldNames.length == 1) {
			sql.append("(");
		}
		sql.append(conditionSql);
		String linkSign = null;
		for (int i = 1; i < size; i++) {
			if (linkSign == null) {
				linkSign = fieldNames.length == 1 ? "," : " or ";
			}
			sql.append(linkSign).append(conditionSql);
		}
		if (fieldNames.length == 1) {
			sql.append(")");
		}
		sql.append(" limit ").append(size);
		return sql.toString();
	}

//	protected void handleItemForTableShard(Object keyItem, Object valueItem, Map<Long, List<ThreeTuple<Object, Object[], Object>>> shardMap) {
//		Long shard;
//		Object[] keyArray = null;
//		if (keyItem instanceof ITuple) {
//			keyArray = ((ITuple) keyItem).toArray();
//			shard = ConverterType.TYPE_LONG.convert(keyArray[0]);
//		} else {
//			shard = ConverterType.TYPE_LONG.convert(keyItem);
//		}
//		shard = getTableShardNumber(shard);
//		//对于最终落到同一个table的sql执行拼接执行
//		List<ThreeTuple<Object, Object[], Object>> olist = shardMap.get(shard);
//		if (olist == null) {
//			olist = CollectionUtil.newArrayList();
//			shardMap.put(shard, olist);
//		}
//		olist.add(Tuple.tuple(keyItem, keyArray, valueItem));
//	}

}
