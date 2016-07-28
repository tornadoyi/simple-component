/**
 * created by haitao.yao @ May 11, 2011
 */
package com.simple.kv.storage.cmem.client;

/**
 * used for cas
 * 
 * @author haitao.yao @ May 11, 2011
 * 
 */
public class CASValue {

	private final Object value;

	public CASValue(Object value, long cas) {
		super();
		this.value = value;
		this.cas = cas;
	}

	private final long cas;

	public Object getValue() {
		return value;
	}

	public long getCas() {
		return cas;
	}

}
