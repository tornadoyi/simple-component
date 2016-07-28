package com.simple.kv.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.simple.base.util.CollectionUtil;

/**
 * java中数据类型信息
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
public class TypeInfo {

	private Class<?> rawClass;
	private List<TypeInfo> genericTypes;
	private TypeInfo arrayType;

	private boolean collection;
	private boolean list;
	private boolean set;
	private boolean map;

	/**
	 * @return 该类型的Class
	 */
	public Class<?> getRawClass() {
		return rawClass;
	}

	/**
	 * @return 该类型的泛型信息
	 */
	public List<TypeInfo> getGenericTypes() {
		return genericTypes;
	}

	/**
	 * @return 如果是数组，则返回该类型的数组类型信息，否则返回null
	 */
	public TypeInfo getArrayType() {
		return arrayType;
	}

	/**
	 * @return 是否为数组
	 */
	public boolean isArray() {
		return arrayType != null;
	}

	/**
	 * @return 是否为不支持的类型
	 */
	public boolean isUnsupportedType() {
		return rawClass == null && !isArray();
	}

	/**
	 * @return 是否为void类型
	 */
	public boolean isVoidType() {
		return rawClass == void.class;
	}

	/**
	 * @return 是否为集合类型
	 */
	public boolean isCollection() {
		return collection;
	}

	/**
	 * @return 是否为List
	 */
	public boolean isList() {
		return list;
	}

	/**
	 * @return 是否为Set
	 */
	public boolean isSet() {
		return set;
	}

	/**
	 * @return 是否为Map
	 */
	public boolean isMap() {
		return map;
	}

	private TypeInfo() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rawClass == null) ? 0 : rawClass.hashCode());
		result = prime * result + ((genericTypes == null) ? 0 : genericTypes.hashCode());
		result = prime * result + ((arrayType == null) ? 0 : arrayType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeInfo other = (TypeInfo) obj;
		if (rawClass == null) {
			if (other.rawClass != null)
				return false;
		} else if (!rawClass.equals(other.rawClass))
			return false;
		if (genericTypes == null) {
			if (other.genericTypes != null)
				return false;
		} else if (!genericTypes.equals(other.genericTypes))
			return false;
		if (arrayType == null) {
			if (other.arrayType != null)
				return false;
		} else if (!arrayType.equals(other.arrayType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "{rawClass:" + (rawClass != null ? rawClass.getName() : null) + ", genericTypes:" + genericTypes + ", arrayType:" + arrayType + "}";
	}

	private static final ConcurrentMap<Type, TypeInfo> tmap = CollectionUtil.newConcurrentMap();

	/**
	 * 取得类型信息
	 * 
	 * @param type
	 *            java中类型的公共高级接口，它们包括Class、ParameterizedType、GenericArrayType等
	 * @return
	 */
	public static TypeInfo getInstance(Type type) {
		TypeInfo info = tmap.get(type);
		if (info == null) {
			info = new TypeInfo();
			if (type instanceof Class<?>) {
				info.rawClass = (Class<?>) type;
			} else if (type instanceof ParameterizedType) {
				ParameterizedType ptype = (ParameterizedType) type;
				info.rawClass = (Class<?>) ptype.getRawType();
				Type[] typeArguments = ptype.getActualTypeArguments();
				info.genericTypes = new ArrayList<TypeInfo>();
				for (Type argType : typeArguments) {
					info.genericTypes.add(getInstance(argType));
				}
			} else if (type instanceof GenericArrayType) {
				GenericArrayType t = (GenericArrayType) type;
				info.arrayType = getInstance(t.getGenericComponentType());
			}
			if (info.rawClass != null) {
				if (info.rawClass.isArray()) {
					info.arrayType = getInstance(info.rawClass.getComponentType());
				} else if (Collection.class.isAssignableFrom(info.rawClass)) {
					info.collection = true;
					if (List.class.isAssignableFrom(info.rawClass)) {
						info.list = true;
					} else if (Set.class.isAssignableFrom(info.rawClass)) {
						info.set = true;
					}
				} else if (Map.class.isAssignableFrom(info.rawClass)) {
					info.map = true;
				}
			}
			TypeInfo old = tmap.putIfAbsent(type, info);
			if (old != null) {
				info = old;
			}
		}
		return info;
	}

}
