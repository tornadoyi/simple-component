package com.simple.kv.storage.serialization;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.error.KVException;
import com.simple.kv.storage.serialization.anno.HeSerializableField;

class HeSerializer implements ISerializer {

	private static interface SimpleWriter<T> {
		void write(T v, HeSerializeContext context);
	}

	private static SimpleWriter<Byte> byteWriter = new SimpleWriter<Byte>() {
		@Override
		public void write(Byte v, HeSerializeContext context) {
			context.write(v);
		}
	};
	private static SimpleWriter<Short> shortWriter = new SimpleWriter<Short>() {
		@Override
		public void write(Short v, HeSerializeContext context) {
			context.write((v >>> 8) & 0xFF);
			context.write((v >>> 0) & 0xFF);
		}
	};
	private static SimpleWriter<Integer> intWriter = new SimpleWriter<Integer>() {
		@Override
		public void write(Integer v, HeSerializeContext context) {
			context.write((v >>> 24) & 0xFF);
			context.write((v >>> 16) & 0xFF);
			context.write((v >>> 8) & 0xFF);
			context.write((v >>> 0) & 0xFF);
		}
	};
	private static SimpleWriter<Long> longWriter = new SimpleWriter<Long>() {
		@Override
		public void write(Long v, HeSerializeContext context) {
			byte[] writeBuffer = new byte[8];
			writeBuffer[0] = (byte) (v >>> 56);
			writeBuffer[1] = (byte) (v >>> 48);
			writeBuffer[2] = (byte) (v >>> 40);
			writeBuffer[3] = (byte) (v >>> 32);
			writeBuffer[4] = (byte) (v >>> 24);
			writeBuffer[5] = (byte) (v >>> 16);
			writeBuffer[6] = (byte) (v >>> 8);
			writeBuffer[7] = (byte) (v >>> 0);
			context.write(writeBuffer, 0, 8);
		}
	};
	private static SimpleWriter<Boolean> booleanWriter = new SimpleWriter<Boolean>() {
		@Override
		public void write(Boolean v, HeSerializeContext context) {
			context.write(v ? 1 : 0);
		}
	};
	private static SimpleWriter<Float> floatWriter = new SimpleWriter<Float>() {
		@Override
		public void write(Float v, HeSerializeContext context) {
			intWriter.write(Float.floatToIntBits(v), context);
		}
	};
	private static SimpleWriter<Double> doubleWriter = new SimpleWriter<Double>() {
		@Override
		public void write(Double v, HeSerializeContext context) {
			longWriter.write(Double.doubleToLongBits(v), context);
		}
	};
	private static SimpleWriter<Character> charWriter = new SimpleWriter<Character>() {
		@Override
		public void write(Character v, HeSerializeContext context) {
			context.write((v >>> 8) & 0xFF);
			context.write((v >>> 0) & 0xFF);
		}
	};
	private static SimpleWriter<String> stringWriter = new SimpleWriter<String>() {
		@Override
		public void write(String v, HeSerializeContext context) {
			try {
				byte[] bytes = v.getBytes("UTF-8");
				if (bytes.length > 65535) {
					throw new KVException("encoded string too long: " + bytes.length + " bytes, " + context.curInfoStr());
				}
				unsignedSmallIntWriter.write(bytes.length, context);
				context.write(bytes, 0, bytes.length);
			} catch (UnsupportedEncodingException e) {
				throw new KVException( "string encoding, " + context.curInfoStr(), e);
			}
		}
	};
	private static SimpleWriter<Integer> tinyIntWriter = new SimpleWriter<Integer>() {
		@Override
		public void write(Integer v, HeSerializeContext context) {
			if (v < -128 || v > 127) {
				throw new KVException("not tinyInt: " + v + ", " + context.curInfoStr());
			}
			context.write(v);
		}
	};
	private static SimpleWriter<Integer> smallIntWriter = new SimpleWriter<Integer>() {
		@Override
		public void write(Integer v, HeSerializeContext context) {
			if (v < -32768 || v > 32767) {
				throw new KVException("not smallInt: " + v + ", " + context.curInfoStr());
			}
			context.write((v >>> 8) & 0xFF);
			context.write((v >>> 0) & 0xFF);
		}
	};
	private static SimpleWriter<Integer> mediumIntWriter = new SimpleWriter<Integer>() {
		@Override
		public void write(Integer v, HeSerializeContext context) {
			if (v < -8388608 || v > 8388607) {
				throw new KVException("not mediumInt: " + v + ", " + context.curInfoStr());
			}
			context.write((v >>> 16) & 0xFF);
			context.write((v >>> 8) & 0xFF);
			context.write((v >>> 0) & 0xFF);
		}
	};
	private static SimpleWriter<Integer> unsignedTinyIntWriter = new SimpleWriter<Integer>() {
		@Override
		public void write(Integer v, HeSerializeContext context) {
			if (v < 0 || v > 255) {
				throw new KVException("not unsignedTinyInt: " + v + ", " + context.curInfoStr());
			}
			context.write(v);
		}
	};
	private static SimpleWriter<Integer> unsignedSmallIntWriter = new SimpleWriter<Integer>() {
		@Override
		public void write(Integer v, HeSerializeContext context) {
			if (v < 0 || v > 65535) {
				throw new KVException("not unsignedSmallInt: " + v + ", " + context.curInfoStr());
			}
			context.write((v >>> 8) & 0xFF);
			context.write((v >>> 0) & 0xFF);
		}
	};
	private static SimpleWriter<Integer> unsignedMediumIntWriter = new SimpleWriter<Integer>() {
		@Override
		public void write(Integer v, HeSerializeContext context) {
			if (v < 0 || v > 16777215) {
				throw new KVException("not unsignedMediumInt: " + v + ", " + context.curInfoStr());
			}
			context.write((v >>> 16) & 0xFF);
			context.write((v >>> 8) & 0xFF);
			context.write((v >>> 0) & 0xFF);
		}
	};
	private static SimpleWriter<Date> dateWriter = new SimpleWriter<Date>() {
		@Override
		public void write(Date v, HeSerializeContext context) {
			intWriter.write((int) (v.getTime() / 1000), context);
		}
	};
	/** 用于写入简单类型数据(非List、非Set、非Map、非数组，非HeSerializable对象)的writer */
	private static final Map<HeSerializableType, SimpleWriter<?>> simpleWriterMap = CollectionUtil.newCopyOnWriteHashMap();
	static {
		simpleWriterMap.put(HeSerializableType.BYTE, byteWriter);
		simpleWriterMap.put(HeSerializableType.SHORT, shortWriter);
		simpleWriterMap.put(HeSerializableType.INT, intWriter);
		simpleWriterMap.put(HeSerializableType.LONG, longWriter);
		simpleWriterMap.put(HeSerializableType.BOOLEAN, booleanWriter);
		simpleWriterMap.put(HeSerializableType.FLOAT, floatWriter);
		simpleWriterMap.put(HeSerializableType.DOUBLE, doubleWriter);
		simpleWriterMap.put(HeSerializableType.CHAR, charWriter);
		simpleWriterMap.put(HeSerializableType.STRING, stringWriter);
		simpleWriterMap.put(HeSerializableType.TINY_INT, tinyIntWriter);
		simpleWriterMap.put(HeSerializableType.SMALL_INT, smallIntWriter);
		simpleWriterMap.put(HeSerializableType.MEDIUM_INT, mediumIntWriter);
		simpleWriterMap.put(HeSerializableType.UNSIGNED_TINY_INT, unsignedTinyIntWriter);
		simpleWriterMap.put(HeSerializableType.UNSIGNED_SMALL_INT, unsignedSmallIntWriter);
		simpleWriterMap.put(HeSerializableType.UNSIGNED_MEDIUM_INT, unsignedMediumIntWriter);
		simpleWriterMap.put(HeSerializableType.DATE, dateWriter);
	}

	private byte[] writeNull() {
		HeSerializeContext context = new HeSerializeContext();
		context.write(HeSerializableType.NULL.getFlag());
		return context.toFinallyByteArray();
	}

	@Override
	public byte[] serializeSimpleData(Object data) {
		return serializeSimpleData(data, null);
	}

	@Override
	public byte[] serializeSimpleData(Object data, HeSerializableType type) {
		if (data == null) {
			return writeNull();
		}
		if (type == null || type == HeSerializableType.DEFAULT) {
			type = HeSerializableType.getHeSerializableType(data.getClass());
		}
		if(type.isSimpleType()) {
			HeSerializeContext context = new HeSerializeContext();
			@SuppressWarnings("unchecked")
			SimpleWriter<Object> writer = (SimpleWriter<Object>) simpleWriterMap.get(type);
			context.write(type.getFlag());
			writer.write(type.getConverterType().convert(data), context);
			return context.toFinallyByteArray();
		} else {
			throw new KVException("writeSimpleData type " + type);
		}
	}

	private static interface ComplexWriter {
		void write(Object v, HeSerializeContext context, Object... params);
	}

	private static ComplexWriter heSerializableObjectWriter = new ComplexWriter() {
		@Override
		public void write(Object v, HeSerializeContext context, Object... params) {
			context.setCurClass(v.getClass().getName()); // set cur class
			context.recordWrittenObject(v); // record written obj
			HeSerializableObjectParser parser = HeSerializableObjectParser.getInstance(v.getClass());
			parser.invokeBeforeMethods(v); // invoke before methods
			unsignedTinyIntWriter.write(parser.getFieldsNum(), context); // write fields num
			for (HeSerializableObjectFieldInfo fieldInfo : parser.getFieldInfos()) {
				HeSerializableField annoField = fieldInfo.getAnnoSerializableField();
				// set cur field
				context.setCurField(fieldInfo.getName());
				context.setCurKey(annoField.key());
				context.write(annoField.key()); // write key
				Object fieldValue = fieldInfo.readValue(v);
				if (fieldValue == null) { // 空字段
					context.write(HeSerializableType.NULL.getFlag()); // write null
					continue;
				}
				TypeInfo fieldTypeInfo = fieldInfo.getTypeInfo();
				HeSerializableType stype = annoField.type();
				if (stype == HeSerializableType.DEFAULT) {
					stype = HeSerializableType.getHeSerializableType(fieldTypeInfo);
				}
				if(stype.isSimpleType()) {
					@SuppressWarnings("unchecked")
					SimpleWriter<Object> simpleWriter = (SimpleWriter<Object>) simpleWriterMap.get(stype);
					context.write(stype.getFlag()); // write flag
					simpleWriter.write(stype.getConverterType().convert(fieldValue), context); // write as simple data
				} else if (context.containsWrittenObject(fieldValue)) {
					context.write(HeSerializableType.REF.getFlag()); // write flag
					context.writeObjectRefOffset(fieldValue); // write as ref
				} else {
					context.write(stype.getFlag()); // write flag
					switch (stype) { // write as complex data
					case ARRAY:
						arrayWriter.write(fieldValue, context, fieldTypeInfo.getArrayType());
						break;
					case LIST:
					case SET:
						List<TypeInfo> gt = fieldTypeInfo.getGenericTypes();
						if (gt == null || gt.size() < 1) {
							throw new KVException("NO_GENERICT_TYPES serializing collection, " + context.curInfoStr());
						}
						collectionWriter.write(fieldValue, context, gt.get(0));
						break;
					case MAP:
						gt = fieldTypeInfo.getGenericTypes();
						if (gt == null || gt.size() < 2) {
							throw new KVException("NO_GENERICT_TYPES serializing map, " + context.curInfoStr());
						}
						mapWriter.write(fieldValue, context, gt.get(0), gt.get(1));
						break;
					case HE_SERIALIZABLE_OBJECT:
						heSerializableObjectWriter.write(fieldValue, context);
						break;
					case SPECIAL_OBJECT:
						SpecialSerializationHandler handler = SpecialSerializationHandlerFactory.getSpecialHandler(fieldTypeInfo.getRawClass());
						specialDataWriter.write(fieldValue, context, handler);
						break;
					default:
						throw new KVException("complex writers, " + context.curInfoStr());
					}
				}
			}
			// clear cur info
			context.setCurClass(null);
			context.setCurField(null);
			context.setCurKey((byte)0);
		}
	};

	private static ComplexWriter specialDataWriter = new ComplexWriter() {
		@Override
		public void write(Object v, HeSerializeContext context, Object... params) {
			context.recordWrittenObject(v); // record written obj
			SpecialSerializationHandler handler = (SpecialSerializationHandler) params[0];
			byte[] bytes = handler.serialize(v);
			if(bytes.length > 65535) {
				throw new KVException("serialized object is too big: " + bytes.length + " bytes, " + context.curInfoStr());
			}
			unsignedSmallIntWriter.write(bytes.length, context);
			if(bytes.length > 0) {
				context.write(bytes, 0, bytes.length);
			}
		}
	};
	
	private static ComplexWriter orderWriter = new ComplexWriter() {
		private Object getItem(Object array, int index, HeSerializeContext context) {
			Object item = array instanceof Object[] ? ((Object[])array)[index] : Array.get(array, index);
			if (item == null) {
				throw new KVException("item in serializing array, " + context.curInfoStr());
			}
			return item;
		}

		@Override
		public void write(Object v, HeSerializeContext context, Object... params) {
			int length = (Integer)params[0];
			TypeInfo itemType = (TypeInfo) params[1];
			if (length == 0) {
				return;
			}
			HeSerializableType stype = HeSerializableType.getHeSerializableType(itemType);
			context.write(stype.getFlag()); // write item flag
			if(stype == HeSerializableType.HE_SERIALIZABLE_OBJECT) { // write as he object
				writeHeSerializableArray(v, length, itemType.getRawClass(), context);
			} else {
				if (stype.isSimpleType()) {
					@SuppressWarnings("unchecked")
					SimpleWriter<Object> simpleWriter = (SimpleWriter<Object>) simpleWriterMap.get(stype);
					for (int i = 0; i < length; i++) {
						simpleWriter.write(stype.getConverterType().convert(getItem(v, i, context)), context); // write as simple item
					}
				} else {
					ComplexWriter complexWriter;
					Object[] args;
					switch (stype) { // write as complex item
					case ARRAY:
						complexWriter = arrayWriter;
						args = new Object[] { itemType.getArrayType() };
						break;
					case LIST:
					case SET:
						List<TypeInfo> gt = itemType.getGenericTypes();
						if (gt == null || gt.size() < 1) {
							throw new KVException("NO_GENERICT_TYPES serializing collection, " + context.curInfoStr());
						}
						complexWriter = collectionWriter;
						args = new Object[] { gt.get(0) };
						break;
					case MAP:
						gt = itemType.getGenericTypes();
						if (gt == null || gt.size() < 2) {
							throw new KVException("NO_GENERICT_TYPES serializing map, " + context.curInfoStr());
						}
						complexWriter = mapWriter;
						args = new Object[] { gt.get(0), gt.get(1) };
						break;
					case SPECIAL_OBJECT:
						complexWriter = specialDataWriter;
						args = new Object[] { SpecialSerializationHandlerFactory.getSpecialHandler(itemType.getRawClass()) };
						break;
					default:
						throw new KVException("complex writers, " + context.curInfoStr());
					}
					for (int i = 0; i < length; i++) {
						complexWriter.write(getItem(v, i, context), context, args);
					}
				}
			}
		}

		final class FieldStruct {
			HeSerializableObjectFieldInfo fieldInfo;
			HeSerializableType stype;
			SimpleWriter<Object> simpleWriter;
			ComplexWriter complexWriter;
			Object[] args;
		}

		private static final int NULL_FLAG = 1;
		private static final int REF_FLAG = 2;

		private void writeHeSerializableArray(Object array, int length, Class<?> itemClass, HeSerializeContext context) {
			HeSerializableObjectParser parser = HeSerializableObjectParser.getInstance(itemClass);
			int fieldsNum = parser.getFieldsNum();
			unsignedTinyIntWriter.write(fieldsNum, context); // write key num
			FieldStruct[] fieldStructs = new FieldStruct[fieldsNum];
			int index = 0;
			for (HeSerializableObjectFieldInfo fieldInfo : parser.getFieldInfos()) {
				HeSerializableField annoField = fieldInfo.getAnnoSerializableField();
				context.write(annoField.key()); // write key
				TypeInfo fieldTypeInfo = fieldInfo.getTypeInfo();
				HeSerializableType stype = annoField.type();
				if (stype == HeSerializableType.DEFAULT) {
					stype = HeSerializableType.getHeSerializableType(fieldTypeInfo);
				}
				context.write(stype.getFlag()); // write flag
				FieldStruct fs = new FieldStruct();
				fs.fieldInfo = fieldInfo;
				fs.stype = stype;
				if (stype.isSimpleType()) {
					@SuppressWarnings("unchecked")
					SimpleWriter<Object> simpleWriter = (SimpleWriter<Object>) simpleWriterMap.get(stype);
					fs.simpleWriter = simpleWriter;
				} else {
					ComplexWriter complexWriter;
					Object[] args;
					switch (stype) {
					case ARRAY:
						complexWriter = arrayWriter;
						args = new Object[] { fieldTypeInfo.getArrayType() };
						break;
					case LIST:
					case SET:
						List<TypeInfo> gt = fieldTypeInfo.getGenericTypes();
						if (gt == null || gt.size() < 1) {
							throw new KVException("NO_GENERICT_TYPES serializing collection, " + context.curInfoStr());
						}
						complexWriter = collectionWriter;
						args = new Object[] { gt.get(0) };
						break;
					case MAP:
						gt = fieldTypeInfo.getGenericTypes();
						if (gt == null || gt.size() < 2) {
							throw new KVException("serializing map, " + context.curInfoStr());
						}
						complexWriter = mapWriter;
						args = new Object[] { gt.get(0), gt.get(1) };
						break;
					case SPECIAL_OBJECT:
						complexWriter = specialDataWriter;
						args = new Object[] { SpecialSerializationHandlerFactory.getSpecialHandler(fieldTypeInfo.getRawClass()) };
						break;
					case HE_SERIALIZABLE_OBJECT:
						complexWriter = heSerializableObjectWriter;
						args = null;
						break;
					default:
						throw new KVException("complex writers, " + context.curInfoStr());
					}
					fs.complexWriter = complexWriter;
					fs.args = args;
				}
				fieldStructs[index++] = fs;
			}
			String mainCls = context.getCurClass();
			String mainField = context.getCurField();
			byte mainKey = context.getCurKey();
			for (int i = 0; i < length; i++) {
				Object item = getItem(array, i, context);
				context.recordWrittenObject(item); // record written obj
				parser.invokeBeforeMethods(item); // invoke before methods
				for (int j = 0; j < fieldsNum; j++) {
					FieldStruct fs = fieldStructs[j];
					// set cur class and field
					context.setCurClass(itemClass.getName());
					context.setCurField(fs.fieldInfo.getName());
					context.setCurKey(fs.fieldInfo.getAnnoSerializableField().key());
					Object fieldValue = fs.fieldInfo.readValue(item);
					int nullOrRef = 0;
					if (fs.stype.canBeNull()) {
						if (fieldValue == null) {
							nullOrRef = NULL_FLAG;
						} else if (context.containsWrittenObject(fieldValue)) {
							nullOrRef = REF_FLAG;
						}
						context.write(nullOrRef); // 对于可以为null或导致循环引用的字段：write null or ref flag
					}
					if (nullOrRef == REF_FLAG) {
						context.writeObjectRefOffset(fieldValue); // write as ref
					} else if (nullOrRef != NULL_FLAG) {
						if (fieldValue == null) {
							throw new KVException("fieldValue " + fs.fieldInfo.getName() + " " + fs.stype + " in HeSerializableObject, " + context.curInfoStr());
						}
						if (fs.simpleWriter != null) {
							fs.simpleWriter.write(fs.stype.getConverterType().convert(fieldValue), context); // write as simple data
						} else { // write as complex data
							fs.complexWriter.write(fieldValue, context, fs.args);
						}
					}
					// restore cur info
					context.setCurClass(mainCls);
					context.setCurField(mainField);
					context.setCurKey(mainKey);
				}
			}

		}
	};
	
	private static ComplexWriter arrayWriter = new ComplexWriter() {
		@Override
		public void write(Object v, HeSerializeContext context, Object... params) {
			context.recordWrittenObject(v); // record written obj
			int length = Array.getLength(v);
			unsignedSmallIntWriter.write(length, context); // write item size
			if(length > 0) {
				orderWriter.write(v, context, length, params[0]);
			}
		}
	};

	private static ComplexWriter collectionWriter = new ComplexWriter() {
		@Override
		public void write(Object v, HeSerializeContext context, Object... params) {
			context.recordWrittenObject(v); // record written obj
			Collection<?> c = (Collection<?>) v;
			int size = c.size();
			unsignedSmallIntWriter.write(size, context); // write item size
			if(size > 0) {
				orderWriter.write(c.toArray(), context, size, params[0]);
			}
		}
	};

	private static ComplexWriter mapWriter = new ComplexWriter() {
		@Override
		public void write(Object v, HeSerializeContext context, Object... params) {
			context.recordWrittenObject(v); // record written obj
			Map<?, ?> m = (Map<?, ?>) v;
			int size = m.size();
			unsignedSmallIntWriter.write(size, context); // write item size
			if (size > 0) {
				TypeInfo keyType = (TypeInfo) params[0];
				TypeInfo valueType = (TypeInfo) params[1];
				Object[] keys = new Object[size], values = new Object[size];
				int index = 0;
				for (Map.Entry<?, ?> entry : m.entrySet()) {
					keys[index] = entry.getKey();
					values[index++] = entry.getValue();
				}
				orderWriter.write(keys, context, size, keyType); // write keys
				orderWriter.write(values, context, size, valueType); // write values
			}
		}
	};

	@Override
	public byte[] serializeHeObject(HeSerializable data) {
		if (data == null) {
			return writeNull();
		}
		HeSerializeContext context = new HeSerializeContext();
		context.write(HeSerializableType.HE_SERIALIZABLE_OBJECT.getFlag());
		heSerializableObjectWriter.write(data, context);
		return context.toFinallyByteArray();
	}

	@Override
	public byte[] serializeSpecialData(Object data) {
		return serializeSpecialData(data, null);
	}

	@Override
	public byte[] serializeSpecialData(Object data, SpecialSerializationHandler handler) {
		if (data == null) {
			return writeNull();
		}
		if(handler == null) {
			handler = SpecialSerializationHandlerFactory.getSpecialHandler(data.getClass());
		}
		HeSerializeContext context = new HeSerializeContext();
		context.write(HeSerializableType.SPECIAL_OBJECT.getFlag());
		specialDataWriter.write(data, context, handler);
		return context.toFinallyByteArray();
	}

	@Override
	public byte[] serializeArray(Object data) {
		if (data == null) {
			return writeNull();
		}
		HeSerializeContext context = new HeSerializeContext();
		context.write(HeSerializableType.ARRAY.getFlag());
		arrayWriter.write(data, context, TypeInfo.getInstance(data.getClass().getComponentType()));
		return context.toFinallyByteArray();
	}

	@Override
	public byte[] serializeArray(Object data, TypeInfo itemType) {
		if (data == null) {
			return writeNull();
		}
		HeSerializeContext context = new HeSerializeContext();
		context.write(HeSerializableType.ARRAY.getFlag());
		arrayWriter.write(data, context, itemType);
		return context.toFinallyByteArray();
	}

	@Override
	public byte[] serializeList(List<?> data, TypeInfo itemType) {
		if (data == null) {
			return writeNull();
		}
		HeSerializeContext context = new HeSerializeContext();
		context.write(HeSerializableType.LIST.getFlag());
		collectionWriter.write(data, context, itemType);
		return context.toFinallyByteArray();
	}

	@Override
	public byte[] serializeSet(Set<?> data, TypeInfo itemType) {
		if (data == null) {
			return writeNull();
		}
		HeSerializeContext context = new HeSerializeContext();
		context.write(HeSerializableType.SET.getFlag());
		collectionWriter.write(data, context, itemType);
		return context.toFinallyByteArray();
	}

	@Override
	public byte[] serializeMap(Map<?, ?> data, TypeInfo keyType, TypeInfo valueType) {
		if (data == null) {
			return writeNull();
		}
		HeSerializeContext context = new HeSerializeContext();
		context.write(HeSerializableType.MAP.getFlag());
		mapWriter.write(data, context, keyType, valueType);
		return context.toFinallyByteArray();
	}

	@Override
	public byte[] serialize(Object data, TypeInfo typeInfo) {
		if(data == null) {
			return writeNull();
		}
		HeSerializableType stype = HeSerializableType.getHeSerializableType(typeInfo);
		if(stype.isSimpleType()) {
			return serializeSimpleData(data, stype);
		}
		switch (stype) {
		case ARRAY:
			return serializeArray(data, typeInfo.getArrayType());
		case LIST:
			List<TypeInfo> gt = typeInfo.getGenericTypes();
			if (gt == null || gt.size() < 1) {
				throw new KVException("NO_GENERICT_TYPES serializing list");
			}
			return serializeList((List<?>)data, gt.get(0));
		case SET:
			gt = typeInfo.getGenericTypes();
			if (gt == null || gt.size() < 1) {
				throw new KVException( "NO_GENERICT_TYPES serializing set");
			}
			return serializeSet((Set<?>)data, gt.get(0));
		case MAP:
			gt = typeInfo.getGenericTypes();
			if (gt == null || gt.size() < 2) {
				throw new KVException("NO_GENERICT_TYPES serializing map");
			}
			return serializeMap((Map<?, ?>)data, gt.get(0), gt.get(1));
		case HE_SERIALIZABLE_OBJECT:
			return serializeHeObject((HeSerializable)data);
		case SPECIAL_OBJECT:
			return serializeSpecialData(data);
		default:
			throw new KVException("NOT_SUPPORTED_TYPE serializing " + stype);
		}
	}

}
