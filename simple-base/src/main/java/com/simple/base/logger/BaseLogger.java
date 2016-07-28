/**
 * 
 */
package com.simple.base.logger;

import org.apache.log4j.Logger;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class BaseLogger {

	private static final String LOGGER = "simple_base";
	public static Logger getLogger(){
		return Logger.getLogger(LOGGER);
	}
	
	public static boolean isDebugEnabled(){
		return getLogger().isDebugEnabled();
	}
	
	public static void debug(String message){
		getLogger().debug(message);
	}
	
	
}
