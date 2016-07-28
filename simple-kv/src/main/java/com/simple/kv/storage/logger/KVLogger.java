/**
 * 
 */
package com.simple.kv.storage.logger;

import org.apache.log4j.Logger;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class KVLogger {

	private static final String LOGGER = "kv_storage";
	public static Logger getLogger(){
		return Logger.getLogger(LOGGER);
	}
}
