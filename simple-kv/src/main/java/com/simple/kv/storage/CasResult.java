package com.simple.kv.storage;

/**
 * 进行get_for_cas操作时的返回结果
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 * @param <T>
 *            返回值value的类型
 */
public class CasResult<T> {
	
	/** not found时的version */
	public static final int NOT_FOUND_VERSION = -1;

	/** not found时的结果 */
	public static final CasResult<Object> NOT_FOUND_RESULT = new CasResult<Object>(NOT_FOUND_VERSION, null);
	
	/**
	 * 用long做version是兼容cmem，实际在mysql实现中int就够了
	 */
	private final long version;
	private final T value;

	public CasResult(long version, T value) {
		this.version = version;
		this.value = value;
	}

	public long getVersion() {
		return version;
	}

	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "CasResult [version=" + version + ", value=" + value + "]";
	}

}
