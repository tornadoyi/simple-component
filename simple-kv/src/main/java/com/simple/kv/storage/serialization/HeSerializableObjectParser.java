package com.simple.kv.storage.serialization;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.simple.base.util.CollectionUtil;
import com.simple.kv.reflect.ReflectUtil;
import com.simple.kv.reflect.TypeInfo;
import com.simple.kv.storage.error.KVException;
import com.simple.kv.storage.serialization.anno.AfterDeserialization;
import com.simple.kv.storage.serialization.anno.BeforeSerialization;
import com.simple.kv.storage.serialization.anno.HeSerializableField;

/**
 * 可序列化的一般对象解析类
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
class HeSerializableObjectParser {

	private final Class<?> objClass;
	private final Map<Byte, HeSerializableObjectFieldInfo> fieldInfoMap;
	private final int fieldsNum;
	private final List<Method> beforeMethods;
	private final List<Method> afterMethods;

	public HeSerializableObjectParser(Class<?> objClass, Map<Byte, HeSerializableObjectFieldInfo> fieldInfoMap, List<Method> beforeMethods, List<Method> afterMethods) {
		this.objClass = objClass;
		this.fieldInfoMap = fieldInfoMap;
		this.fieldsNum = fieldInfoMap.size();
		if (fieldsNum > 255) {
			throw new KVException("RANGE_ERROR fieldsNum");
		}
		this.beforeMethods = beforeMethods;
		this.afterMethods = afterMethods;
	}

	/**
	 * 获取字段信息的集合
	 * 
	 * @return
	 */
	public Collection<HeSerializableObjectFieldInfo> getFieldInfos() {
		return fieldInfoMap.values();
	}

	/**
	 * 获取某个字段的信息
	 * 
	 * @param key
	 *            字段的key
	 * @return
	 */
	public HeSerializableObjectFieldInfo getFieldInfo(Byte key) {
		return fieldInfoMap.get(key);
	}

	/**
	 * 获取字段key列表
	 * 
	 * @return 字段key列表
	 */
	public Set<Byte> getFieldKeysSet() {
		return fieldInfoMap.keySet();
	}

	/**
	 * 获取字段数目
	 * 
	 * @return
	 */
	public int getFieldsNum() {
		return fieldsNum;
	}

	/**
	 * 获取在序列化之前执行的方法
	 * 
	 * @return
	 */
	public List<Method> getBeforeMethods() {
		return beforeMethods;
	}

	/**
	 * 获取在发序列化之后执行的方法
	 * 
	 * @return
	 */
	public List<Method> getAfterMethods() {
		return afterMethods;
	}
	
	/**
	 * 调用在序列化之前执行的方法
	 * @param obj 对象
	 */
	public void invokeBeforeMethods(Object obj) {
		for(Method m : beforeMethods) {
			invoke(m, obj);
		}
	}
	
	/**
	 * 调用在发序列化之后执行的方法
	 * @param obj 对象
	 */
	public void invokeAfterMethods(Object obj) {
		for(Method m : afterMethods) {
			invoke(m, obj);
		}
	}
	
	private void invoke(Method m, Object obj) {
		try {
			m.invoke(obj);
		} catch (IllegalArgumentException e) {
			throw new KVException(m.getDeclaringClass() + "." + m.getName() + " " + Arrays.toString(m.getParameterTypes()));
		} catch (IllegalAccessException e) {
			throw new KVException(m.getDeclaringClass() + "." + m.getName());
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if(t == null) {
				t = e;
			}
			throw new KVException(m.getDeclaringClass() + "." + m.getName(), t);
		}
	}

	/**
	 * 创建 一个新对象，要求改对象的class必须有可见的默认构造函数
	 * 
	 * @return 新对象
	 */
	@SuppressWarnings("unchecked")
	public <T> T newInstance() {
		return (T)ReflectUtil.createInstance(objClass);
	}

	private static final Map<Class<?>, HeSerializableObjectParser> parserCache = CollectionUtil.newCopyOnWriteHashMap();

	/**
	 * 获取对象解析器
	 * 
	 * @param objClass
	 *            对象的Class
	 * @return 构造好的parser
	 */
	public static HeSerializableObjectParser getInstance(Class<?> objClass) {
		HeSerializableObjectParser parser = parserCache.get(objClass);
		if (parser == null) {
			synchronized (objClass) {
				parser = parserCache.get(objClass);
				if (parser == null) {
					parser = parseObject(objClass);
					parserCache.put(objClass, parser);
				}
			}
		}
		return parser;
	}

	private static Comparator<Integer> orderComparator = new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			if (o1 > o2) {
				return 1;
			} else if (o1 < o2) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	private static HeSerializableObjectParser parseObject(Class<?> objClass) {
		List<Class<?>> clss = CollectionUtil.newArrayList();
		Class<?> scls = objClass;
		do {
			clss.add(scls);
		} while ((scls = scls.getSuperclass()) != null && scls != Object.class);
		Map<Byte, HeSerializableObjectFieldInfo> fieldInfoMap = CollectionUtil.newTreeMap();
		for (Class<?> cls : clss) {
			Field[] fields = cls.getDeclaredFields();
			for (Field fd : fields) {
				int modifiers = fd.getModifiers();
				if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
					continue;
				}
				HeSerializableField annoField = fd.getAnnotation(HeSerializableField.class);
				if (annoField != null) { // 只有标记了该注解的字段才能被序列化
					HeSerializableObjectFieldInfo f = new HeSerializableObjectFieldInfo();
					f.setName(fd.getName());
					f.setField(fd);
					f.setTypeInfo(TypeInfo.getInstance(fd.getGenericType()));
					f.setAnnoSerializableField(annoField);
					if(annoField.key() < 0 || annoField.key() > 127) {
						throw new KVException("RANGE_ERROR field key");
					}
					HeSerializableObjectFieldInfo of = fieldInfoMap.put(annoField.key(), f);
					if (of != null) {
						throw new KVException("field key " + annoField.key());
					}
				}
			}
		}
		LinkedList<Method> beforeMethods = CollectionUtil.newLinkedList();
		LinkedList<Method> afterMethods = CollectionUtil.newLinkedList();
		Method[] methods = objClass.getDeclaredMethods();
		Map<Integer, List<Method>> befores = new TreeMap<Integer, List<Method>>(orderComparator);
		Map<Integer, List<Method>> afters = new TreeMap<Integer, List<Method>>(orderComparator);
		for (Method m : methods) {
			BeforeSerialization annoBefore = m.getAnnotation(BeforeSerialization.class);
			if (annoBefore != null) {
				List<Method> l = befores.get(annoBefore.order());
				if (l == null) {
					l = CollectionUtil.newArrayList();
					befores.put(annoBefore.order(), l);
				}
				m.setAccessible(true);
				l.add(m);
			}
			AfterDeserialization annoAfter = m.getAnnotation(AfterDeserialization.class);
			if (annoAfter != null) {
				List<Method> l = afters.get(annoAfter.order());
				if (l == null) {
					l = CollectionUtil.newArrayList();
					afters.put(annoAfter.order(), l);
				}
				m.setAccessible(true);
				l.add(m);
			}
		}
		for (List<Method> l : befores.values()) {
			beforeMethods.addAll(l);
		}
		for (List<Method> l : afters.values()) {
			afterMethods.addAll(l);
		}
		return new HeSerializableObjectParser(objClass, fieldInfoMap, beforeMethods, afterMethods);
	}
}
