/**
 * $Id$
 * Copyright(C) 2010-2016 happyelements.com. All rights reserved.
 */
package com.simple.base.jedis;

import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import com.simple.base.config.XmlConfig;
import com.simple.base.util.CollectionUtil;

/**
 * 
 * @author <a href="mailto:yongliang.zhao@happyelements.com">yongliang.zhao</a>
 * @version 1.0
 * @since 1.0
 */
public class JedisConfig {

	public static JedisConfig getConfig(String instance) {
		return JedisConfigMgr.getInstance().getConfig(instance);
	}
	
	public static void main(String[] args) {
		System.out.println(JedisConfig.getConfig("game_account").getHost());
	}

	@Attribute
	private String name;

	@Attribute
	private String host;

	@Attribute
	private int port;

	@Attribute(name = "max_idel", required = false)
	private int maxIdel = 300;

	@Attribute(name = "max_wait", required = false)
	private int maxWait = 300000;

	@Attribute(name = "pool_time_wait", required = false)
	private int poolTimeWait = 300000;

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	/**
	 * 取得maxIdel
	 * 
	 * @return the maxIdel
	 */
	public int getMaxIdel() {
		return maxIdel;
	}

	/**
	 * 取得maxWait
	 * 
	 * @return the maxWait
	 */
	public int getMaxWait() {
		return maxWait;
	}

	/**
	 * 取得poolTimeWait
	 * 
	 * @return the poolTimeWait
	 */
	public int getPoolTimeWait() {
		return poolTimeWait;
	}

	/**
	 * 取得name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}

@Root(name = "redis_conf")
class JedisConfigMgr {

	public static JedisConfigMgr getInstance() {
		return XmlConfig.getConfig(JedisConfigMgr.class);
	}

	@ElementList(inline = true, entry = "instance")
	private List<JedisConfig> configList = CollectionUtil.newArrayList();

	private Map<String, JedisConfig> configMap = CollectionUtil.newHashMap();

	@Commit
	public void init() {
		for (JedisConfig config : configList) {
			if (configMap.containsKey(config.getName())) {
				throw new RuntimeException("REDIS_CONFIG_ERROR");
			}
			configMap.put(config.getName(), config);
		}
	}

	public JedisConfig getConfig(String instance) {
		return configMap.get(instance);
	}
}
