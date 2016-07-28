/**
 * 
 */
package com.simple.kv.storage.error;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class KVException extends RuntimeException {

	/**  **/
	private static final long serialVersionUID = 6287365128231679787L;

	/**
	 * 
	 */
	public KVException() {
	}

	/**
	 * @param message
	 */
	public KVException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public KVException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public KVException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public KVException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
