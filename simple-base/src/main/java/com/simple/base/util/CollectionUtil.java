package com.simple.base.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 集合工具类
 */
public class CollectionUtil {
	
	/**
	 * 创建一个新的java.util.HashMap对象
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @return 新创建的java.util.HashMap对象
	 */
	public static final <K,V> Map<K,V> newHashMap() {
		return new HashMap<K,V>();
	}
	
	/**
	 * 创建一个新的java.util.HashMap对象
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @return 新创建的java.util.HashMap对象
	 */
	public static final <K,V> Map<K,V> newHashMap(Map<K,V> map) {
		return new HashMap<K,V>(map);
	}
	
	/**
	 * 创建一个新的java.util.TreeMap对象
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @return 新创建的java.util.TreeMap对象
	 */
	public static final <K,V> SortedMap<K,V> newTreeMap() {
		return new TreeMap<K,V>();
	}
	
	/**
	 * 创建一个新的java.util.LinkedHashMap对象
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @return 新创建的java.util.LinkedHashMap对象
	 */
	public static final <K, V> Map<K, V> newLinkedHashMap() {
		return new LinkedHashMap<K, V>();
	}
	
	/**
	 * 创建一个新的java.util.concurrent.ConcurrentMap对象
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @return 新创建的java.util.concurrent.ConcurrentMap对象
	 */
	public static final <K,V> ConcurrentMap<K,V> newConcurrentMap() {
		return new ConcurrentHashMap<K,V>();
	}
	
	
	/**
	 * 创建一个新的CopyOnWriteHashMap对象
	 * @param <K> Key类型
	 * @param <V> Value类型
	 * @return 新创建的CopyOnWriteHashMap对象
	 */
	public static final <K,V> CopyOnWriteHashMap<K, V> newCopyOnWriteHashMap() {
		return new CopyOnWriteHashMap<K, V>();
	}

	/**
	 * 创建一个新的java.util.HashSet对象
	 * @param <E> Value类型
	 * @return 新创建的java.util.HashSet对象
	 */
	public static final <E> Set<E> newHashSet() {
		return new HashSet<E>();
	}
	
	/**
	 * 创建一个新的java.util.TreeSet对象
	 * @param <E> Value类型
	 * @return 新创建的java.util.TreeSet对象
	 */
	public static final <E> SortedSet<E> newTreeSet() {
		return new TreeSet<E>();
	}
	
	/**
	 * 创建一个新的java.util.LinkedHashSet对象
	 * @param <E> Value类型
	 * @return 新创建的java.util.LinkedHashSet对象
	 */
	public static final <E> Set<E> newLinkedHashSet() {
		return new LinkedHashSet<E>();
	}
	
	/**
	 * 创建一个新的java.util.ArrayList对象
	 * @param <E> Value类型
	 * @return 新创建的java.util.ArrayList对象
	 */
	public static final <E> List<E> newArrayList() {
		return new ArrayList<E>();
	}

	/**
	 * 创建一个新的java.util.ArrayList对象
	 * @param <E> Value类型
	 * @return 新创建的java.util.ArrayList对象
	 */
	public static final <E> List<E> newArrayList(int size) {
		return new ArrayList<E>(size);
	}
	
	/**
	 * 创建一个新的java.util.LinkedList对象
	 * @param <E> Value类型
	 * @return 新创建的java.util.LinkedList对象
	 */
	public static final <E> LinkedList<E> newLinkedList() {
		return new LinkedList<E>();
	}
	
	/**
	 * 创建一个新的数组
	 * @param <T> Value类型
	 * @param t Value
	 * @return 新创建的数组
	 */
	public static <T> T[] newArray(T... t) {
	    return t;
	}
	
	/**
     * Null-safe check if the specified collection is empty.
     * 
     * @param coll  the collection to check, may be null
     * @return true if empty or null
     */
    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }
    
    /**
     * Null-safe check if the specified collection is empty.
     * 
     * @param coll  the collection to check, may be null
     * @return true if empty or null
     */
    public static boolean isEmpty(Map<?,?> coll) {
    	return (coll == null || coll.isEmpty());
    }
    
    
    
}
