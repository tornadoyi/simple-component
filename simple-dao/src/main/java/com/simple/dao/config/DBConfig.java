/**
 * 
 */
package com.simple.dao.config;

import java.util.List;

/**
 * 获取数据源配置的接口
 * 在应用时需实现在这个接口接受数据源配置
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public interface DBConfig {
	public List<DataSourceConfig> getDbSourceConfigList();
	
}
