/**
 * 
 */
package com.simple.base.xmltest;

import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSON;
import com.simple.base.config.XmlConfig;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月21日
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(Main.class.getResource("log4j.properties"));
	    System.setProperty(XmlConfig.XML_CONFIG_DIR_PATH_KAY, "D:/spiral_account/simple-component/simple-base/src/test/java/com/simple/base/xmltest");
	    DbConf dbConfig = XmlConfig.getConfig(DbConf.class);
	    System.out.println(JSON.toJSONString(dbConfig));
	}

}
