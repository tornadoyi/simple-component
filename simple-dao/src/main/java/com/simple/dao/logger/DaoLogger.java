/**
 * 
 */
package com.simple.dao.logger;

import org.apache.log4j.Logger;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class DaoLogger {

	private static final String DAO_LOGGER = "com.simple.dao";
	public static Logger getDaoLogger(){
		return Logger.getLogger(DAO_LOGGER);
	}
}
