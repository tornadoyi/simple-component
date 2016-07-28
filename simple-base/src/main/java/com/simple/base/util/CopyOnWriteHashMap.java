package com.simple.base.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 线程安全的hashmap，基于copy on write实现，在读操作时不上锁，适合读多写很少的情况
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 * @param <K>
 * @param <V>
 */
public class CopyOnWriteHashMap<K, V> implements Map<K, V>, Cloneable, Serializable {

	private static final long serialVersionUID = -5280736106839631610L;

	private volatile HashMap<K, V> map = null;

	// Constructors
	// ----------------------------------------------------------------------

	/**
	 * Construct an empty map.
	 */
	public CopyOnWriteHashMap() {
		this.map = new HashMap<K, V>();
	}

	public CopyOnWriteHashMap(Map<K, V> map) {
		this.map = new HashMap<K, V>(map);
	}

	// Map access
	// ----------------------------------------------------------------------
	// These methods can forward straight to the wrapped Map ( no lock )

	public V get(Object key) {
		return map.get(key);
	}

	public int size() {
		return (map.size());
	}

	public boolean isEmpty() {
		return (map.isEmpty());
	}

	public boolean containsKey(Object key) {
		return (map.containsKey(key));
	}

	public boolean containsValue(Object value) {
		return (map.containsValue(value));
	}

	// Map modification
	// ----------------------------------------------------------------------
	// These methods perform special behaviour.
	// The map is cloned, updated and then assigned back.

	public V put(K key, V value) {
		synchronized (this) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			HashMap<K, V> temp = (HashMap) map.clone();
			V result = temp.put(key, value);
			map = temp;
			return (result);
		}
	}

	public void putAll(Map<? extends K, ? extends V> in) {
		synchronized (this) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			HashMap<K, V> temp = (HashMap) map.clone();
			temp.putAll(in);
			map = temp;
		}
	}

	public V remove(Object key) {
		synchronized (this) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			HashMap<K, V> temp = (HashMap) map.clone();
			V result = temp.remove(key);
			map = temp;
			return (result);
		}
	}

	public void clear() {
		synchronized (this) {
			map = new HashMap<K, V>();
		}
	}

	// Basic object methods
	// ----------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o == this) {
			return (true);
		} else if (!(o instanceof Map)) {
			return (false);
		}
		@SuppressWarnings("rawtypes")
		Map<K, V> mo = (Map) o;
		mo.equals(mo);
		if (mo.size() != map.size()) {
			return (false);
		}
		Iterator<Entry<K, V>> i = map.entrySet().iterator();
		while (i.hasNext()) {
			Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			if (value == null) {
				if (!(mo.get(key) == null && mo.containsKey(key))) {
					return (false);
				}
			} else {
				if (!value.equals(mo.get(key))) {
					return (false);
				}
			}
		}
		return (true);
	}

	public int hashCode() {
		int h = 0;
		Iterator<Entry<K, V>> i = map.entrySet().iterator();
		while (i.hasNext()) {
			h += i.next().hashCode();
		}
		return (h);
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		CopyOnWriteHashMap<K, V> result = null;
		try {
			result = (CopyOnWriteHashMap<K, V>) super.clone();
		} catch (CloneNotSupportedException e) {
			// assert false;
		}
		result.map = new HashMap<K, V>(map);
		return result;
	}

	// Map views
	// ----------------------------------------------------------------------

	public Set<Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	public Set<K> keySet() {
		return new KeySet();
	}

	public Collection<V> values() {
		return new Values();
	}

	// Map view inner classes
	// ----------------------------------------------------------------------

	/**
	 * Abstract collection implementation shared by keySet(), values() and
	 * entrySet().
	 */
	private abstract class CollectionView<E> implements Collection<E> {

		public CollectionView() {
		}

		protected abstract Collection<E> get(Map<K, V> map);

		protected abstract E iteratorNext(Entry<K, V> entry);

		public void clear() {
			synchronized (CopyOnWriteHashMap.this) {
				map = new HashMap<K, V>();
			}
		}

		public boolean remove(Object o) {
			synchronized (CopyOnWriteHashMap.this) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				HashMap<K, V> temp = (HashMap) map.clone();
				boolean r = get(temp).remove(o);
				map = temp;
				return r;
			}
		}

		public boolean removeAll(Collection<?> o) {
			synchronized (CopyOnWriteHashMap.this) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				HashMap<K, V> temp = (HashMap) map.clone();
				boolean r = get(temp).removeAll(o);
				map = temp;
				return r;
			}
		}

		public boolean retainAll(Collection<?> o) {
			synchronized (CopyOnWriteHashMap.this) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				HashMap<K, V> temp = (HashMap) map.clone();
				boolean r = get(temp).retainAll(o);
				map = temp;
				return r;
			}
		}

		public int size() {
			return get(map).size();
		}

		public boolean isEmpty() {
			return get(map).isEmpty();
		}

		public boolean contains(Object o) {
			return get(map).contains(o);
		}

		public boolean containsAll(Collection<?> o) {
			return get(map).containsAll(o);
		}

		public <G> G[] toArray(G[] o) {
			return get(map).toArray(o);
		}

		public Object[] toArray() {
			return get(map).toArray();
		}

		public boolean equals(Object o) {
			if (o == this)
				return true;
			return get(map).equals(o);
		}

		public int hashCode() {
			return get(map).hashCode();
		}

		public boolean add(E o) {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends E> c) {
			throw new UnsupportedOperationException();
		}

		public Iterator<E> iterator() {
			return new CollectionViewIterator<E>();
		}

		private class CollectionViewIterator<A> implements Iterator<A> {

			private Map<K, V> expected;
			private Map.Entry<K, V> lastReturned = null;
			private Iterator<Entry<K, V>> iterator;

			public CollectionViewIterator() {
				this.expected = map;
				this.iterator = expected.entrySet().iterator();
			}

			public boolean hasNext() {
				if (expected != map) {
					throw new ConcurrentModificationException();
				}
				return iterator.hasNext();
			}

			@SuppressWarnings("unchecked")
			public A next() {
				if (expected != map) {
					throw new ConcurrentModificationException();
				}
				lastReturned = iterator.next();
				return (A) iteratorNext(lastReturned);
			}

			public void remove() {
				if (lastReturned == null) {
					throw new IllegalStateException();
				}
				synchronized (CopyOnWriteHashMap.this) {
					if (expected != map) {
						throw new ConcurrentModificationException();
					}
					CopyOnWriteHashMap.this.remove(lastReturned.getKey());
					lastReturned = null;
					expected = map;
				}
			}
		}
	}

	private class KeySet extends CollectionView<K> implements Set<K> {

		protected Collection<K> get(Map<K, V> map) {
			return map.keySet();
		}

		protected K iteratorNext(Entry<K, V> entry) {
			return entry.getKey();
		}

	}

	private class Values extends CollectionView<V> {

		protected Collection<V> get(Map<K, V> map) {
			return map.values();
		}

		protected V iteratorNext(Entry<K, V> entry) {
			return entry.getValue();
		}
	}

	private class EntrySet extends CollectionView<Entry<K, V>> implements Set<Entry<K, V>> {

		protected Collection<Entry<K, V>> get(Map<K, V> map) {
			return map.entrySet();
		}

		protected Entry<K, V> iteratorNext(Entry<K, V> entry) {
			return entry;
		}

	}

}
