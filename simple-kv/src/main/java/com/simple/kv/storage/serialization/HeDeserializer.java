package com.simple.kv.storage.serialization;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.converter.ConverterType;
import com.simple.kv.storage.converter.IConverter;
import com.simple.kv.storage.error.KVException;
import com.simple.kv.storage.serialization.anno.HeSerializableField;

class HeDeserializer implements IDeserializer {

	private static interface SimpleReader<T> {
		T read(HeDeserializeContext context);
	}

	private static SimpleReader<Byte> byteReader = new SimpleReader<Byte>() {
		@Override
		public Byte read(HeDeserializeContext context) {
			int ch = context.read();
			if (ch < 0) {
				throw new KVException("EOF_ERROR deserializing");
			}
			return (byte) (ch);
		}
	};
	private static SimpleReader<Short> shortReader = new SimpleReader<Short>() {
		@Override
		public Short read(HeDeserializeContext context) {
			int ch1 = context.read();
			int ch2 = context.read();
			if ((ch1 | ch2) < 0)
				throw new KVException("EOF_ERROR deserializing");
			return (short) ((ch1 << 8) + (ch2 << 0));
		}
	};
	private static SimpleReader<Integer> intReader = new SimpleReader<Integer>() {
		@Override
		public Integer read(HeDeserializeContext context) {
			int ch1 = context.read();
			int ch2 = context.read();
			int ch3 = context.read();
			int ch4 = context.read();
			if ((ch1 | ch2 | ch3 | ch4) < 0)
				throw new KVException("EOF_ERROR deserializing");
			return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
		}
	};
	private static SimpleReader<Long> longReader = new SimpleReader<Long>() {
		@Override
		public Long read(HeDeserializeContext context) {
			byte readBuffer[] = new byte[8];
			context.readFully(readBuffer, 0, 8);
			return (((long) readBuffer[0] << 56) +
					((long) (readBuffer[1] & 255) << 48) +
					((long) (readBuffer[2] & 255) << 40) +
					((long) (readBuffer[3] & 255) << 32) +
					((long) (readBuffer[4] & 255) << 24) +
					((readBuffer[5] & 255) << 16) +
					((readBuffer[6] & 255) << 8) +
			((readBuffer[7] & 255) << 0));
		}
	};
	private static SimpleReader<Boolean> booleanReader = new SimpleReader<Boolean>() {
		@Override
		public Boolean read(HeDeserializeContext context) {
			int ch = context.read();
			if (ch < 0)
				throw new KVException("EOF_ERROR deserializing");
			return (ch != 0);
		}
	};
	private static SimpleReader<Float> floatReader = new SimpleReader<Float>() {
		@Override
		public Float read(HeDeserializeContext context) {
			return Float.intBitsToFloat(intReader.read(context));
		}
	};
	private static SimpleReader<Double> doubleReader = new SimpleReader<Double>() {
		@Override
		public Double read(HeDeserializeContext context) {
			return Double.longBitsToDouble(longReader.read(context));
		}
	};
	private static SimpleReader<Character> charReader = new SimpleReader<Character>() {
		public Character read(HeDeserializeContext context) {
			int ch1 = context.read();
			int ch2 = context.read();
			if ((ch1 | ch2) < 0)
				throw new KVException("EOF_ERROR deserializing");
			return (char) ((ch1 << 8) + (ch2 << 0));
		}
	};
	private static SimpleReader<String> stringReader = new SimpleReader<String>() {
		@Override
		public String read(HeDeserializeContext context) {
			try {
				int len = unsignedSmallIntReader.read(context);
				byte[] bytes = new byte[len];
				context.readFully(bytes, 0, len);
				return new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new KVException(" UNKNOWN_ERROR string encoding", e);
			}
		}
	};
	private static SimpleReader<Integer> tinyIntReader = new SimpleReader<Integer>() {
		@Override
		public Integer read(HeDeserializeContext context) {
			int ch = context.read();
			if (ch < 0)
				throw new KVException("EOF_ERROR deserializing");
			if ((0x80 & ch) == 0x80) {
				ch = 0xffffff00 | ch;
			}
			return ch;
		}
	};
	private static SimpleReader<Integer> smallIntReader = new SimpleReader<Integer>() {
		@Override
		public Integer read(HeDeserializeContext context) {
			int ch1 = context.read();
			int ch2 = context.read();
			if ((ch1 | ch2) < 0)
				throw new KVException("EOF_ERROR deserializing");
			int ch = (ch1 << 8) + (ch2 << 0);
			if ((0x8000 & ch) == 0x8000) {
				ch = 0xffff0000 | ch;
			}
			return ch;
		}
	};
	private static SimpleReader<Integer> mediumIntReader = new SimpleReader<Integer>() {
		@Override
		public Integer read(HeDeserializeContext context) {
			int ch1 = context.read();
			int ch2 = context.read();
			int ch3 = context.read();
			if ((ch1 | ch2 | ch3) < 0)
				throw new KVException("EOF_ERROR deserializing");
			int ch = (ch1 << 16) + (ch2 << 8) + (ch3 << 0);
			if ((0x800000 & ch) == 0x800000) {
				ch = 0xff000000 | ch;
			}
			return ch;
		}
	};
	private static SimpleReader<Integer> unsignedTinyIntReader = new SimpleReader<Integer>() {
		@Override
		public Integer read(HeDeserializeContext context) {
			int ch = context.read();
			if (ch < 0)
				throw new KVException("EOF_ERROR deserializing");
			return ch;
		}
	};
	private static SimpleReader<Integer> unsignedSmallIntReader = new SimpleReader<Integer>() {
		@Override
		public Integer read(HeDeserializeContext context) {
			int ch1 = context.read();
			int ch2 = context.read();
			if ((ch1 | ch2) < 0)
				throw new KVException("EOF_ERROR deserializing");
			return (ch1 << 8) + (ch2 << 0);
		}
	};
	private static SimpleReader<Integer> unsignedMediumIntReader = new SimpleReader<Integer>() {
		@Override
		public Integer read(HeDeserializeContext context) {
			int ch1 = context.read();
			int ch2 = context.read();
			int ch3 = context.read();
			if ((ch1 | ch2 | ch3) < 0)
				throw new KVException("EOF_ERROR deserializing");
			return (ch1 << 16) + (ch2 << 8) + (ch3 << 0);
		}
	};
	private static SimpleReader<Date> dateReader = new SimpleReader<Date>() {
		@Override
		public Date read(HeDeserializeContext context) {
			int m = intReader.read(context);
			return new Date(m * 1000L);
		}
	};
	/** 用于写入简单类型数据(非List、非Set、非Map、非数组，非HeSerializable对象)的Reader */
	private static final Map<HeSerializableType, SimpleReader<?>> simpleReaderMap = CollectionUtil.newCopyOnWriteHashMap();
	static {
		simpleReaderMap.put(HeSerializableType.BYTE, byteReader);
		simpleReaderMap.put(HeSerializableType.SHORT, shortReader);
		simpleReaderMap.put(HeSerializableType.INT, intReader);
		simpleReaderMap.put(HeSerializableType.LONG, longReader);
		simpleReaderMap.put(HeSerializableType.BOOLEAN, booleanReader);
		simpleReaderMap.put(HeSerializableType.FLOAT, floatReader);
		simpleReaderMap.put(HeSerializableType.DOUBLE, doubleReader);
		simpleReaderMap.put(HeSerializableType.CHAR, charReader);
		simpleReaderMap.put(HeSerializableType.STRING, stringReader);
		simpleReaderMap.put(HeSerializableType.TINY_INT, tinyIntReader);
		simpleReaderMap.put(HeSerializableType.SMALL_INT, smallIntReader);
		simpleReaderMap.put(HeSerializableType.MEDIUM_INT, mediumIntReader);
		simpleReaderMap.put(HeSerializableType.UNSIGNED_TINY_INT, unsignedTinyIntReader);
		simpleReaderMap.put(HeSerializableType.UNSIGNED_SMALL_INT, unsignedSmallIntReader);
		simpleReaderMap.put(HeSerializableType.UNSIGNED_MEDIUM_INT, unsignedMediumIntReader);
		simpleReaderMap.put(HeSerializableType.DATE, dateReader);
	}

	private static HeSerializableType readFlag(HeDeserializeContext context) {
		byte flag = byteReader.read(context);
		return HeSerializableType.getHeSerializableType(flag);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserializeSimpleData(byte[] bytes) {
		return (T) deserializeSimpleData(bytes, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserializeSimpleData(byte[] bytes, HeSerializableType type) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		if (type == null || type == HeSerializableType.DEFAULT) {
			type = stype;
		}
		SimpleReader<Object> reader = (SimpleReader<Object>) simpleReaderMap.get(stype);
		if (reader != null) {
			Object r = reader.read(context);
			if (type != stype) {
				r = type.getConverterType().convert(r);
			}
			return (T) r;
		} else {
			throw new KVException(" NOT_SUPPORTED_TYPE readSimpleData type " + stype);
		}
	}

	private static interface ComplexReader {
		<T> T read(HeDeserializeContext context, Object... params);
	}

	private static ComplexReader heSerializableObjectReader = new ComplexReader() {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T read(HeDeserializeContext context, Object... params) {
			// 不传targetClass的时候表明为skip模式
			Class<?> targetClass = params.length > 0 ? (Class<?>) params[0] : null;
			boolean skipMode = targetClass == null;
			HeSerializableObjectParser parser = !skipMode ? HeSerializableObjectParser.getInstance(targetClass) : null;
			Object v = !skipMode ? parser.newInstance() : null;
			if (!skipMode) {
				context.recordReadObject(v); // record read obj
			}
			int fieldNum = unsignedTinyIntReader.read(context); // read field Num
			for (int i = 0; i < fieldNum; i++) {
				byte key = byteReader.read(context); // read key
				HeSerializableType stype = readFlag(context); // read flag
				if (stype == HeSerializableType.NULL) { // may be null
					continue;
				}
				Object fieldValue;
				HeSerializableObjectFieldInfo fieldInfo = !skipMode ? parser.getFieldInfo(key) : null;
				boolean skipField = fieldInfo == null; // fieldInfo may not be exist
				TypeInfo fieldTypeInfo = !skipField ? fieldInfo.getTypeInfo() : null;
				HeSerializableField annoField = !skipField ? fieldInfo.getAnnoSerializableField() : null;
				if (stype == HeSerializableType.REF) { // as ref
					fieldValue = context.readObjectAsRef();
				} else {
					SimpleReader<Object> simpleReader = (SimpleReader<Object>) simpleReaderMap.get(stype);
					if (simpleReader != null) { // as simple data
						fieldValue = simpleReader.read(context);
						if (!skipField) {
							fieldValue = ConverterType.getConverter(fieldTypeInfo).convert(fieldValue);
						} // else skip not exist field
					} else {
						switch (stype) { // as complex data
						case HE_SERIALIZABLE_OBJECT:
							if (!skipField) {
								fieldValue = heSerializableObjectReader.read(context, annoField.clazz() != void.class ? annoField.clazz() : fieldTypeInfo.getRawClass());
							} else {
								fieldValue = heSerializableObjectReader.read(context); // skip not exist field
							}
							break;
						case ARRAY:
							if (!skipField) {
								fieldValue = arrayReader.read(context, fieldTypeInfo.getArrayType());
							} else {
								fieldValue = arrayReader.read(context); // skip not exist field
							}
							break;
						case LIST:
							if (!skipField) {
								List<TypeInfo> gt = fieldTypeInfo.getGenericTypes();
								if (gt == null || gt.size() < 1) {
									throw new KVException("NO_GENERICT_TYPES deserializing list");
								}
								Class<?> listClass = annoField.clazz() != void.class ? annoField.clazz() : fieldTypeInfo.getRawClass();
								fieldValue = listReader.read(context, gt.get(0), listClass);
							} else {
								fieldValue = listReader.read(context);
							}
							break;
						case SET:
							if (!skipField) {
								List<TypeInfo> gt = fieldTypeInfo.getGenericTypes();
								if (gt == null || gt.size() < 1) {
									throw new KVException("NO_GENERICT_TYPES deserializing set");
								}
								Class<?> setClass = annoField.clazz() != void.class ? annoField.clazz() : fieldTypeInfo.getRawClass();
								fieldValue = setReader.read(context, gt.get(0), setClass);
							} else {
								fieldValue = setReader.read(context);
							}
							break;
						case MAP:
							if (!skipField) {
								List<TypeInfo> gt = fieldTypeInfo.getGenericTypes();
								if (gt == null || gt.size() < 2) {
									throw new KVException("NO_GENERICT_TYPES deserializing map");
								}
								Class<?> mapClass = annoField.clazz() != void.class ? annoField.clazz() : fieldTypeInfo.getRawClass();
								fieldValue = mapReader.read(context, gt.get(0), gt.get(1), mapClass);
							} else {
								fieldValue = mapReader.read(context);
							}
							break;
						case SPECIAL_OBJECT:
							if (!skipField) {
								SpecialSerializationHandler handler = SpecialSerializationHandlerFactory.getSpecialHandler(fieldTypeInfo.getRawClass());
								fieldValue = specialDataReader.read(context, handler);
							} else {
								fieldValue = specialDataReader.read(context); // skip not exist field
							}
							break;
						default:
							throw new KVException("SOMETHING_NOT_FOUND complex readers");
						}
					}

				}
				if (!skipField) {
					fieldInfo.writeValue(v, fieldValue);
				}
			}
			if (!skipMode) {
				parser.invokeAfterMethods(v); // invoke after methods
			}
			return (T) v;
		}
	};

	private static ComplexReader specialDataReader = new ComplexReader() {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T read(HeDeserializeContext context, Object... params) {
			SpecialSerializationHandler handler = params.length > 0 ? (SpecialSerializationHandler) params[0] : null;
			// handler为null表明为skip模式
			boolean skipMode = handler == null;
			int offset = context.getReadBytesNum();
			int len = unsignedSmallIntReader.read(context);
			if (len > 0) {
				byte[] bytes = new byte[len];
				context.readFully(bytes, 0, len);
				if (!skipMode) {
					T obj = (T) handler.deserialize(bytes);
					context.recordReadObject(obj, offset); // record read obj
					return obj;
				}
			}
			return null;
		}
	};

	private static ComplexReader orderReader = new ComplexReader() {
		private void setItem(Object array, int index, Object item) {
			if (array instanceof Object[]) {
				((Object[]) array)[index] = item;
			} else {
				Array.set(array, index, item);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T read(HeDeserializeContext context, Object... params) {
			int length = (Integer) params[0]; // 必须有length
			// array为空的时候表明为skip模式
			Object array = params.length > 1 ? params[1] : null;
			boolean skipMode = array == null;
			TypeInfo itemType = !skipMode ? (TypeInfo) params[2] : null; // 非skip模式必须有itemType
			if (length == 0) {
				return (T) array;
			}
			HeSerializableType stype = readFlag(context); // read item flag
			if (stype == HeSerializableType.HE_SERIALIZABLE_OBJECT) { // read as he object
				readHeSerializableArray(context, array, length, !skipMode ? itemType.getRawClass() : null);
			} else {
				SimpleReader<Object> simpleReader = (SimpleReader<Object>) simpleReaderMap.get(stype);
				if (simpleReader != null) { // read as simple item
					IConverter<?> converter = !skipMode ? ConverterType.getConverter(itemType) : null;
					for (int i = 0; i < length; i++) {
						Object item = simpleReader.read(context);
						if (!skipMode) {
							item = converter.convert(item);
							setItem(array, i, item);
						}
					}
				} else {
					ComplexReader complexReader;
					Object[] args;
					switch (stype) { // read as complex item
					case ARRAY:
						complexReader = arrayReader;
						args = !skipMode ? new Object[] { itemType.getArrayType() } : new Object[0];
						break;
					case LIST:
						complexReader = listReader;
						if (!skipMode) {
							List<TypeInfo> gt = itemType.getGenericTypes();
							if (gt == null || gt.size() < 1) {
								throw new KVException("NO_GENERICT_TYPES deserializing list");
							}
							args = new Object[] { gt.get(0), itemType.getRawClass() };
						} else {
							args = new Object[0];
						}
						break;
					case SET:
						complexReader = setReader;
						if (!skipMode) {
							List<TypeInfo> gt = itemType.getGenericTypes();
							if (gt == null || gt.size() < 1) {
								throw new KVException("NO_GENERICT_TYPES deserializing set");
							}
							args = new Object[] { gt.get(0), itemType.getRawClass() };
						} else {
							args = new Object[0];
						}
						break;
					case MAP:
						complexReader = mapReader;
						if (!skipMode) {
							List<TypeInfo> gt = itemType.getGenericTypes();
							if (gt == null || gt.size() < 2) {
								throw new KVException("NO_GENERICT_TYPES deserializing map");
							}
							args = new Object[] { gt.get(0), gt.get(1), itemType.getRawClass() };
						} else {
							args = new Object[0];
						}
						break;
					case SPECIAL_OBJECT:
						complexReader = specialDataReader;
						args = !skipMode ? new Object[] { SpecialSerializationHandlerFactory.getSpecialHandler(itemType.getRawClass()) } : new Object[0];
						break;
					default:
						throw new KVException("SOMETHING_NOT_FOUND complex readers");
					}
					for (int i = 0; i < length; i++) {
						Object item = complexReader.read(context, args);
						if (!skipMode) {
							setItem(array, i, item);
						}
					}
				}
			}

			return (T) array;
		}

		final class FieldStruct {
			boolean skipField;
			HeSerializableObjectFieldInfo fieldInfo;
			HeSerializableType stype;
			SimpleReader<Object> simpleReader;
			ComplexReader complexReader;
			Object[] args;
		}

		private static final int NULL_FLAG = 1;
		private static final int REF_FLAG = 2;

		private void readHeSerializableArray(HeDeserializeContext context, Object array, int length, Class<?> itemClass) {
			// array为null表明为skip模式
			boolean skipMode = array == null;
			HeSerializableObjectParser parser = !skipMode ? HeSerializableObjectParser.getInstance(itemClass) : null;
			int fieldsNum = unsignedTinyIntReader.read(context); // read key num
			FieldStruct[] fieldStructs = new FieldStruct[fieldsNum];
			for (int i = 0; i < fieldsNum; i++) {
				byte key = byteReader.read(context); // read key
				HeSerializableType stype = readFlag(context); // read flag
				FieldStruct fs = new FieldStruct();
				fs.stype = stype;
				fs.fieldInfo = !skipMode ? parser.getFieldInfo(key) : null;
				fs.skipField = fs.fieldInfo == null; // fieldInfo may not be exist
				HeSerializableField annoField = !fs.skipField ? fs.fieldInfo.getAnnoSerializableField() : null;
				TypeInfo fieldTypeInfo = !fs.skipField ? fs.fieldInfo.getTypeInfo() : null;
				@SuppressWarnings("unchecked")
				SimpleReader<Object> simpleReader = (SimpleReader<Object>) simpleReaderMap.get(stype);
				if (simpleReader != null) {
					fs.simpleReader = simpleReader;
				} else {
					ComplexReader complexReader;
					Object[] args;
					switch (stype) {
					case ARRAY:
						complexReader = arrayReader;
						args = !fs.skipField ? new Object[] { fieldTypeInfo.getArrayType() } : new Object[0];
						break;
					case LIST:
						complexReader = listReader;
						if (!fs.skipField) {
							List<TypeInfo> gt = fieldTypeInfo.getGenericTypes();
							if (gt == null || gt.size() < 1) {
								throw new KVException("NO_GENERICT_TYPES deserializing list");
							}
							args = new Object[] { gt.get(0), annoField.clazz() != void.class ? annoField.clazz() : fieldTypeInfo.getRawClass() };
						} else {
							args = new Object[0];
						}
						break;
					case SET:
						complexReader = setReader;
						if (!fs.skipField) {
							List<TypeInfo> gt = fieldTypeInfo.getGenericTypes();
							if (gt == null || gt.size() < 1) {
								throw new KVException("NO_GENERICT_TYPES deserializing set");
							}
							args = new Object[] { gt.get(0), annoField.clazz() != void.class ? annoField.clazz() : fieldTypeInfo.getRawClass() };
						} else {
							args = new Object[0];
						}
						break;
					case MAP:
						complexReader = mapReader;
						if (!fs.skipField) {
							List<TypeInfo> gt = fieldTypeInfo.getGenericTypes();
							if (gt == null || gt.size() < 2) {
								throw new KVException("NO_GENERICT_TYPES deserializing map");
							}
							args = new Object[] { gt.get(0), gt.get(1), annoField.clazz() != void.class ? annoField.clazz() : fieldTypeInfo.getRawClass() };
						} else {
							args = new Object[0];
						}
						break;
					case SPECIAL_OBJECT:
						complexReader = specialDataReader;
						args = !fs.skipField ? new Object[] { SpecialSerializationHandlerFactory.getSpecialHandler(fieldTypeInfo.getRawClass()) } : new Object[0];
						break;
					case HE_SERIALIZABLE_OBJECT:
						complexReader = heSerializableObjectReader;
						args = !fs.skipField ? new Object[] { annoField.clazz() != void.class ? annoField.clazz() : fieldTypeInfo.getRawClass() } : new Object[0];
						break;
					default:
						throw new KVException("SOMETHING_NOT_FOUND complex readers");
					}
					fs.complexReader = complexReader;
					fs.args = args;
				}
				fieldStructs[i] = fs;
			}
			for (int i = 0; i < length; i++) {
				Object item = !skipMode ? parser.newInstance() : null;
				if (!skipMode) {
					context.recordReadObject(item); // record read obj
				}
				for (int j = 0; j < fieldsNum; j++) {
					FieldStruct fs = fieldStructs[j];
					Object fieldValue;
					int nullOrRef = 0;
					if (fs.stype.canBeNull()) {
						nullOrRef = tinyIntReader.read(context); // read null or ref flag for some field
					}
					if (nullOrRef == REF_FLAG) {
						fieldValue = context.readObjectAsRef(); // read as ref
					} else if (nullOrRef != NULL_FLAG) {
						if (fs.simpleReader != null) { // read as simple data
							fieldValue = fs.simpleReader.read(context);
							if (!fs.skipField) {
								fieldValue = fs.stype.getConverterType().convert(fieldValue);
							}
						} else { // read as complex data
							fieldValue = fs.complexReader.read(context, fs.args);
						}
					} else {
						fieldValue = null;
					}
					if (!fs.skipField) {
						fs.fieldInfo.writeValue(item, fieldValue);
					}
				}
				if (!skipMode) {
					parser.invokeAfterMethods(item); // invoke after methods
					setItem(array, i, item);
				}
			}
		}
	};

	private static ComplexReader arrayReader = new ComplexReader() {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T read(HeDeserializeContext context, Object... params) {
			int offset = context.getReadBytesNum();
			int length = unsignedSmallIntReader.read(context); // read item size
			TypeInfo itemType = params.length > 0 ? (TypeInfo) params[0] : null;
			boolean skipMode = itemType == null;
			Object array = !skipMode ? Array.newInstance(itemType.getRawClass() != null ? itemType.getRawClass() : Object.class, length) : null;
			if (!skipMode) {
				context.recordReadObject(array, offset); // record read obj
			}
			if (length > 0) {
				orderReader.read(context, length, array, itemType); // read array
			}
			return (T) array;
		}
	};

	private static abstract class CollectionReader implements ComplexReader {
		protected abstract <T extends Collection<?>> T getCollectionImplement(Class<T> cls);

		@SuppressWarnings("unchecked")
		@Override
		public <T> T read(HeDeserializeContext context, Object... params) {
			int offset = context.getReadBytesNum();
			int length = unsignedSmallIntReader.read(context); // read item size
			TypeInfo itemType = params.length > 0 ? (TypeInfo) params[0] : null;
			boolean skipMode = itemType == null;
			Collection<Object> col = !skipMode ? getCollectionImplement(params.length > 1 ? (Class<? extends Collection<Object>>) params[1] : null) : null;
			if (!skipMode) {
				context.recordReadObject(col, offset); // record read obj
			}
			if (length > 0) {
				Object[] array = !skipMode ? new Object[length] : null;
				orderReader.read(context, length, array, itemType); // read array
				if (!skipMode) {
					for (int i = 0; i < length; i++) { // translate to collection
						col.add(array[i]);
					}
				}
			}
			return (T) col;
		}
	}

	private static ComplexReader listReader = new CollectionReader() {
		@SuppressWarnings("unchecked")
		@Override
		protected <T extends Collection<?>> T getCollectionImplement(Class<T> cls) {
			if (cls != null) {
				int mod = cls.getModifiers();
				if (!Modifier.isInterface(mod) && !Modifier.isAbstract(mod)) {
					try {
						return (T) cls.newInstance();
					} catch (Exception e) {
						throw new KVException("UNKNOWN_ERROR create List " + cls, e);
					}
				}
				if (AbstractSequentialList.class.isAssignableFrom(cls)) {
					return (T) CollectionUtil.newLinkedList();
				}
			}
			return (T) CollectionUtil.newArrayList();
		}

	};

	private static ComplexReader setReader = new CollectionReader() {
		@SuppressWarnings("unchecked")
		@Override
		protected <T extends Collection<?>> T getCollectionImplement(Class<T> cls) {
			if (cls != null) {
				int mod = cls.getModifiers();
				if (!Modifier.isInterface(mod) && !Modifier.isAbstract(mod)) {
					try {
						return (T) cls.newInstance();
					} catch (Exception e) {
						throw new KVException("UNKNOWN_ERROR create set " + cls, e);
					}
				}
				if (SortedSet.class.isAssignableFrom(cls)) {
					return (T) CollectionUtil.newTreeSet();
				}
			}
			return (T) CollectionUtil.newHashSet();
		}
	};

	private static ComplexReader mapReader = new ComplexReader() {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T read(HeDeserializeContext context, Object... params) {
			int offset = context.getReadBytesNum();
			int length = unsignedSmallIntReader.read(context); // read item size
			TypeInfo keyType = params.length > 0 ? (TypeInfo) params[0] : null;
			boolean skipMode = keyType == null;
			Map<Object, Object> map = null;
			TypeInfo valueType = null;
			Object[] keyArray = null;
			Object[] valueArray = null;
			if (!skipMode) {
				valueType = (TypeInfo) params[1];
				keyArray = new Object[length];
				valueArray = new Object[length];
				Class<?> mapCls = params.length > 2 ? (Class<?>) params[2] : null;
				if (mapCls != null) {
					int mod = mapCls.getModifiers();
					if (!Modifier.isInterface(mod) && !Modifier.isAbstract(mod)) {
						try {
							map = (Map<Object, Object>) mapCls.newInstance();
						} catch (Exception e) {
							throw new KVException("UNKNOWN_ERROR create Map " + mapCls, e);
						}
					}
					if (map == null) {
						if (ConcurrentMap.class.isAssignableFrom(mapCls)) {
							map = CollectionUtil.newConcurrentMap();
						} else if (ConcurrentNavigableMap.class.isAssignableFrom(mapCls)) {
							map = new ConcurrentSkipListMap<Object, Object>();
						} else if (SortedMap.class.isAssignableFrom(mapCls)) {
							map = CollectionUtil.newTreeMap();
						}
					}
				}
				if (map == null) {
					map = CollectionUtil.newHashMap();
				}
				context.recordReadObject(map, offset); // record read obj
			}
			if (length > 0) {
				orderReader.read(context, length, keyArray, keyType); // read key array
				orderReader.read(context, length, valueArray, valueType); // read value array
				if (!skipMode) {
					for (int i = 0; i < length; i++) {
						map.put(keyArray[i], valueArray[i]);
					}
				}
			}
			return (T) map;
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public <T extends HeSerializable> T deserializeHeObject(byte[] bytes, Class<T> targetClass) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		return (T) heSerializableObjectReader.read(context, targetClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserializeSpecialData(byte[] bytes, Class<T> targetClass) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		SpecialSerializationHandler handler = SpecialSerializationHandlerFactory.getSpecialHandler(targetClass);
		return (T) specialDataReader.read(context, handler);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserializeSpecialData(byte[] bytes, SpecialSerializationHandler handler) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		return (T) specialDataReader.read(context, handler);
	}

	@Override
	public Object deserializeArray(byte[] bytes, Class<?> arrayClass) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		return arrayReader.read(context, TypeInfo.getInstance(arrayClass.getComponentType()));
	}

	@Override
	public Object deserializeArray(byte[] bytes, TypeInfo itemType) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		return arrayReader.read(context, itemType);
	}

	@Override
	public <T> List<T> deserializeList(byte[] bytes, TypeInfo itemType, Class<?> targetClass) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		return listReader.read(context, itemType, targetClass);
	}

	@Override
	public <T> Set<T> deserializeSet(byte[] bytes, TypeInfo itemType, Class<?> targetClass) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		return setReader.read(context, itemType, targetClass);
	}

	@Override
	public <K, V> Map<K, V> deserializeMap(byte[] bytes, TypeInfo keyType, TypeInfo valueType, Class<?> targetClass) {
		HeDeserializeContext context = new HeDeserializeContext(bytes);
		HeSerializableType stype = readFlag(context);
		if (stype == HeSerializableType.NULL) {
			return null;
		}
		return mapReader.read(context, keyType, valueType, targetClass);
	}

	@Override
	public <T> List<T> deserializeList(byte[] bytes, TypeInfo itemType) {
		return deserializeList(bytes, itemType, null);
	}

	@Override
	public <T> Set<T> deserializeSet(byte[] bytes, TypeInfo itemType) {
		return deserializeSet(bytes, itemType, null);
	}

	@Override
	public <K, V> Map<K, V> deserializeMap(byte[] bytes, TypeInfo keyType, TypeInfo valueType) {
		return deserializeMap(bytes, keyType, valueType, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object deserialize(byte[] bytes, TypeInfo typeInfo) {
		HeSerializableType stype = HeSerializableType.getHeSerializableType(typeInfo);
		if (stype.isSimpleType()) {
			return deserializeSimpleData(bytes, stype);
		}

		switch (stype) {
		case ARRAY:
			return deserializeArray(bytes, typeInfo.getArrayType());
		case LIST:
			List<TypeInfo> gt = typeInfo.getGenericTypes();
			if (gt == null || gt.size() < 1) {
				throw new KVException("NO_GENERICT_TYPES serializing list");
			}
			return deserializeList(bytes, gt.get(0), typeInfo.getRawClass());
		case SET:
			gt = typeInfo.getGenericTypes();
			if (gt == null || gt.size() < 1) {
				throw new KVException("NO_GENERICT_TYPES serializing set");
			}
			return deserializeSet(bytes, gt.get(0), typeInfo.getRawClass());
		case MAP:
			gt = typeInfo.getGenericTypes();
			if (gt == null || gt.size() < 2) {
				throw new KVException("NO_GENERICT_TYPES serializing map");
			}
			return deserializeMap(bytes, gt.get(0), gt.get(1), typeInfo.getRawClass());
		case HE_SERIALIZABLE_OBJECT:
			return deserializeHeObject(bytes, (Class<? extends HeSerializable>) typeInfo.getRawClass());
		case SPECIAL_OBJECT:
			return deserializeSpecialData(bytes, typeInfo.getRawClass());
		default:
			throw new KVException("NOT_SUPPORTED_TYPE serializing " + stype);
		}
	}

}
