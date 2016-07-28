/**
 * 
 */
package com.simple.dao.config.impl.simplexml;

import java.util.ArrayList;
import java.util.List;

import com.simple.base.config.XmlConfig;
import com.simple.dao.config.TableConfig;
import com.simple.dao.config.TablePartition;
import com.simple.dao.logger.DaoLogger;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月26日
 */
public class TableConfigXml implements TableConfig {

	/* (non-Javadoc)
	 * @see com.simple.dao.config.TableConfig#getTablePartitionList()
	 */
	@Override
	public List<TablePartition> getTablePartitionList() {
		final TableConf conf = XmlConfig.getConfig(TableConf.class);
		if(conf == null){
			DaoLogger.getDaoLogger().debug("table config not found");
			return null;
		}		
		
		List<TablePartition> list = new ArrayList<TablePartition>();
		
		for(PartitionInfo partition : conf.getPartitionInfoList()){
			TablePartition entry = new TablePartition();
			entry.setKeyColumn(partition.getKeyColumn());
			entry.setPartitionRule(partition.getPartitionRule());
			entry.setTableName(partition.getTableName());
			entry.setTableNameFormat(partition.getTableNameFormat());
			list.add(entry);
		}
		
		return list;
	}

}
