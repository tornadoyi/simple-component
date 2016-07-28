/**
 * 
 */
package com.simple.dao.config.impl.simplexml;

import java.util.ArrayList;
import java.util.List;

import com.simple.base.config.XmlConfig;
import com.simple.dao.bean.DBUnitInfo;
import com.simple.dao.config.DBConfig;
import com.simple.dao.config.DataSourceConfig;
import com.simple.dao.logger.DaoLogger;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月25日
 */
public class DBConfigXml implements DBConfig {

	/* (non-Javadoc)
	 * @see com.simple.dao.config.DBConfig#getDbSourceConfigList()
	 */
	@Override
	public List<DataSourceConfig> getDbSourceConfigList() {
		
		final DbConf conf = XmlConfig.getConfig(DbConf.class);
		
		
		if(conf == null){
			DaoLogger.getDaoLogger().error("load config faild  from DBConfigXml");
			return null;
		}		
		List<DataSourceConfig> dsList = new ArrayList<DataSourceConfig>();
		for(DbInstance db : conf.getInstanceList()){
			DataSourceConfig datasource = new DataSourceConfig();
			datasource.setName(db.getName());
			
			List<DBUnitInfo> unitList = new ArrayList<DBUnitInfo>();
			
			for(Server server : db.getServerList()){
				DBUnitInfo unit = new DBUnitInfo();
				unit.setHost(server.getHost());
				unit.setPassword(server.getPassword());
				unit.setDbName(server.getDatabase());
				unit.setRead(server.isRead());
				unit.setInitPoolSize(server.getInitPoolSize());
				unit.setMaxPoolSize(server.getMaxPoolSize());
				unit.setWrite(server.isWrite());
				unit.setUserName(server.getUser());
				unitList.add(unit);
			}
			datasource.setUnitList(unitList);
			dsList.add(datasource);
		}
		return dsList;
	}

}
