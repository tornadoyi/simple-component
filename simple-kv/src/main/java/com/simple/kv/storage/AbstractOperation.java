package com.simple.kv.storage;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.simple.base.util.ArrayUtil;
import com.simple.base.util.CollectionUtil;
import com.simple.base.util.tuple.ITuple;
import com.simple.base.util.tuple.TwoTuple;
import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.error.KVException;
import com.simple.kv.storage.logger.KVLogger;
import com.simple.kv.storage.serialization.HeSerializingFactory;
import com.simple.kv.storage.serialization.IDeserializer;
import com.simple.kv.storage.serialization.ISerializer;

/**
 * 所有操作对象都可以继承自此抽象类，复用该类中定义的构造函数
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public abstract class AbstractOperation implements IOperation {
	
	protected final StorageInfo info;
	protected final int keyFieldLength;
	protected final Method method;
	protected final TypeInfo[] paramTypes;
	protected final TypeInfo returnType;

	protected boolean needReturnBoolean;
	protected TypeInfo resultItemType;

	private static final int MAX_STORAGE_SIZE = 60000; // 支持的最大存储

	protected static interface SqlFieldDispose {
		public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException;

		public Object parse(ResultSet rs, String fieldName) throws SQLException;
	}

	protected SqlFieldDispose[] sqlFieldDisposes;


	public AbstractOperation(StorageInfo info, Method method) {
		this.info = info;
		this.keyFieldLength = info.getKeyParamInfo().second.length;
		this.method = method;
		this.paramTypes = new TypeInfo[method.getParameterTypes().length];
		for (int i = 0; i < paramTypes.length; i++) {
			paramTypes[i] = TypeInfo.getInstance(method.getGenericParameterTypes()[i]);
		}
		this.returnType = TypeInfo.getInstance(method.getGenericReturnType());
		checkKeyParamType();
		checkValueParamType();
		checkReturnType();

		
		this.resultItemType = returnType;
	}

//	protected long getTableShardNumber(long shard) {
//		return virtualTable != null ? virtualTable.getShardRule().getTableShardRule().getShardResult(shard) : -1;
//	}

	private static final Map<Class<?>, SqlFieldDispose> fieldDisposeMap = CollectionUtil.newHashMap();
	static {
		fieldDisposeMap.put(boolean.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setBoolean(index, (Boolean) fieldValue);
			}

			@Override
			public Boolean parse(ResultSet rs, String fieldName) throws SQLException {
				return rs.getBoolean(fieldName);
			}
		});
		fieldDisposeMap.put(char.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setString(index, fieldValue.toString());
			}

			@Override
			public Character parse(ResultSet rs, String fieldName) throws SQLException {
				return ConverterType.TYPE_CHARACTER.convert(rs.getString(fieldName));
			}
		});
		fieldDisposeMap.put(byte.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setByte(index, (Byte) fieldValue);
			}

			@Override
			public Byte parse(ResultSet rs, String fieldName) throws SQLException {
				return rs.getByte(fieldName);
			}
		});
		fieldDisposeMap.put(short.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setShort(index, (Short) fieldValue);
			}

			@Override
			public Short parse(ResultSet rs, String fieldName) throws SQLException {
				return rs.getShort(fieldName);
			}
		});
		fieldDisposeMap.put(int.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setInt(index, (Integer) fieldValue);
			}

			@Override
			public Integer parse(ResultSet rs, String fieldName) throws SQLException {
				return rs.getInt(fieldName);
			}
		});
		fieldDisposeMap.put(long.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setLong(index, (Long) fieldValue);
			}

			@Override
			public Long parse(ResultSet rs, String fieldName) throws SQLException {
				return rs.getLong(fieldName);
			}
		});
		fieldDisposeMap.put(float.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setFloat(index, (Float) fieldValue);
			}

			@Override
			public Float parse(ResultSet rs, String fieldName) throws SQLException {
				return rs.getFloat(fieldName);
			}
		});
		fieldDisposeMap.put(double.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setDouble(index, (Double) fieldValue);
			}

			@Override
			public Double parse(ResultSet rs, String fieldName) throws SQLException {
				return rs.getDouble(fieldName);
			}
		});
		fieldDisposeMap.put(Boolean.class, fieldDisposeMap.get(boolean.class));
		fieldDisposeMap.put(Character.class, fieldDisposeMap.get(char.class));
		fieldDisposeMap.put(Byte.class, fieldDisposeMap.get(byte.class));
		fieldDisposeMap.put(Short.class, fieldDisposeMap.get(short.class));
		fieldDisposeMap.put(Integer.class, fieldDisposeMap.get(int.class));
		fieldDisposeMap.put(Long.class, fieldDisposeMap.get(long.class));
		fieldDisposeMap.put(Float.class, fieldDisposeMap.get(float.class));
		fieldDisposeMap.put(Double.class, fieldDisposeMap.get(double.class));
		fieldDisposeMap.put(String.class, new SqlFieldDispose() {
			@Override
			public void setParam(PreparedStatement ps, int index, Object fieldValue) throws SQLException {
				ps.setString(index, fieldValue.toString());
			}

			@Override
			public String parse(ResultSet rs, String fieldName) throws SQLException {
				return rs.getString(fieldName);
			}
		});
	}

	@Override
	public void setResultItemType(TypeInfo typeInfo) {
		this.resultItemType = typeInfo;
	}

	protected void checkKeyItemType(TypeInfo keyType) {
		Class<?> kcls = keyType.getRawClass();
		if (kcls == null) {
			throw new KVException("STORAGE_METHOD_DECLARE_ERROR unsupported keyItem type " + keyType);
		}
		if (KeyParamAware.class.isAssignableFrom(kcls)) {
			try {
				keyType = TypeInfo.getInstance(kcls.getDeclaredMethod("readKeyParam").getGenericReturnType());
				kcls = keyType.getRawClass();
			} catch (Exception e) {
				throw new KVException(e);
			}
		}
		List<Class<?>> kclslist = CollectionUtil.newArrayList();
		if (ITuple.class.isAssignableFrom(kcls)) {
			List<TypeInfo> gt = keyType.getGenericTypes();
			for (TypeInfo tmp : gt) {
				kclslist.add(tmp.getRawClass());
			}
		} else {
			kclslist.add(kcls);
		}
		if (kclslist.size() != keyFieldLength) {
			throw new KVException("INCONSISTENT_FIELDNAMES " + method.getDeclaringClass().getName() + "." + method.getName());
		}
		sqlFieldDisposes = new SqlFieldDispose[keyFieldLength];
		for (int i = 0; i < keyFieldLength; i++) {
			Class<?> tmp = kclslist.get(i);
			if (fieldDisposeMap.containsKey(tmp)) {
				sqlFieldDisposes[i] = fieldDisposeMap.get(tmp);
			} else {
				throw new KVException("STORAGE_METHOD_DECLARE_ERROR unsupported keyParam type " + keyType);
			}
		}
	}

	protected void checkKeyParamType() {
		checkKeyItemType(paramTypes[info.getKeyParamInfo().first]);
	}

	protected void checkValueParamType() {
	}

	protected void checkReturnType() {
		switch (info.getType()) {
		case GET:
			if (returnType.isVoidType()) {
				throw new KVException("STORAGE_METHOD_DECLARE_ERROR need return type for get operation");
			}
			break;
		case GET_FOR_CAS:
			if (returnType.getRawClass() == null || returnType.getRawClass() != CasResult.class) {
				throw new KVException("STORAGE_METHOD_DECLARE_ERROR error return type for get_for_cas operation " + returnType);
			}
			break;
		case CAS:
			if (returnType.getRawClass() == null || returnType.getRawClass() != CasStatus.class) {
				throw new KVException("STORAGE_METHOD_DECLARE_ERROR error return type for cas operation " + returnType);
			}
			break;
		default:
			needReturnBoolean = returnType.getRawClass() == boolean.class || returnType.getRawClass() == Boolean.class;
			if (!needReturnBoolean && returnType.getRawClass() != void.class) {
				throw new KVException("STORAGE_METHOD_DECLARE_ERROR only allow void or boolean return type for " + info.getType());
			}
			break;
		}
	}

	@SuppressWarnings("unchecked")
	protected Object getKeyParam4Single(Object[] args) {
		TwoTuple<Integer, String[]> keyParamInfo = info.getKeyParamInfo();
		Object keyParam = args[keyParamInfo.first];
		if (keyParam instanceof KeyParamAware) {
			keyParam = ((KeyParamAware<Object>) keyParam).readKeyParam();
		}
		if (keyParam == null) {
			throw new IllegalArgumentException("storage key can not be null");
		}
		return keyParam;
	}

	protected String buildWhereSqlChip() {
		String[] fieldNames = info.getKeyParamInfo().second;
		String _sqlChip = "from " + info.getTableName() + " where ";
		for (int i = 0; i < fieldNames.length; i++) {
			if (i > 0) {
				_sqlChip += " and ";
			}
			_sqlChip += "`" + fieldNames[i] + "`=?";
		}
		_sqlChip += " limit 1";
		return _sqlChip;
	}

	protected int setParamForKeyItem(PreparedStatement ps, Object keyItem, int index) throws SQLException {
		if (keyItem instanceof Object[]) {
			int fi = 0;
			for (Object kp : (Object[]) keyItem) {
				sqlFieldDisposes[fi++].setParam(ps, index++, kp);
			}
		} else {
			sqlFieldDisposes[0].setParam(ps, index++, keyItem);
		}
		return index;
	}

	@SuppressWarnings("unchecked")
	protected Object parseResult(ResultSet rs, Object keyParam) throws SQLException {
		byte[] bytes = rs.getBytes("value");
		if (bytes != null) {
			Object value = decode(keyParam, bytes, resultItemType);
			if (value instanceof KeyParamAware) {
				((KeyParamAware<Object>) value).writeKeyParam(keyParam);
			}
			return value;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected Object parseResult(Object keyParam, byte[] result) {
		Object value = this.decode(keyParam, result, resultItemType);
		if (value instanceof KeyParamAware) {
			((KeyParamAware<Object>) value).writeKeyParam(keyParam);
		}
		return value;
	}

	// for cache or cmem
	protected String buildRealKey(Object keyParam) {
		String realKey;
		if (keyParam instanceof ITuple) { // union key
			realKey = info.getKeyPrefix() + "_" + ArrayUtil.join(((ITuple) keyParam).toArray(), "_");
		} else { // single key
			realKey = info.getKeyPrefix() + "_" + keyParam.toString();
		}
		if (info.isDailyExpired()) {
			realKey += "_" + StorageConfigUtil.getExpiredWeekDay(info.getStorageConfig());
		}
		
		KVLogger.getLogger().debug("realKey: " + realKey);
		
		return realKey;
	}

	private ISerializer serializer = HeSerializingFactory.getSerializer();
	private IDeserializer deserializer = HeSerializingFactory.getDeserializer();

	protected byte[] encode(Object keyParam, Object value, TypeInfo typeInfo) {
		byte[] bs = StorageContext.getEncodedObject(keyParam);
		if (bs == null) {
			bs = serializer.serialize(value, typeInfo);
			if (bs.length > MAX_STORAGE_SIZE) {
				throw new KVException("MAX_STORAGE_SIZE " + MAX_STORAGE_SIZE+ " "+ bs.length);
			}
			StorageContext.setEncodedObject(keyParam, bs);
		}
		return bs;
	}

	protected Object decode(Object keyParam, byte[] bytes, TypeInfo typeInfo) {
		if(bytes == null) {
			return null;
		}
		StorageContext.setEncodedObject(keyParam, bytes);
		return deserializer.deserialize(bytes, typeInfo);
	}



}
