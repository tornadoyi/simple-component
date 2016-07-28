/**
 * 
 */
package com.simple.dao.error;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class DaoException extends RuntimeException {

	/**  **/
	private static final long serialVersionUID = 6287365128231679787L;

	/**
	 * 
	 */
	public DaoException() {
	}

	/**
	 * @param message
	 */
	public DaoException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DaoException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DaoException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
