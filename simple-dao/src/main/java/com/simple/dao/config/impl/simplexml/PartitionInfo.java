/**
 * 
 */
package com.simple.dao.config.impl.simplexml;

import org.simpleframework.xml.Element;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月26日
 */
public class PartitionInfo {
	@Element(name = "tableName")
	private String tableName;
	@Element(name = "keyColumn")
	private String keyColumn;
	@Element(name = "partitionRule")
	private String partitionRule;
	@Element(name = "tableNameFormat")
	private String tableNameFormat;
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getKeyColumn() {
		return keyColumn;
	}
	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}
	public String getPartitionRule() {
		return partitionRule;
	}
	public void setPartitionRule(String partitionRule) {
		this.partitionRule = partitionRule;
	}
	public String getTableNameFormat() {
		return tableNameFormat;
	}
	public void setTableNameFormat(String tableNameFormat) {
		this.tableNameFormat = tableNameFormat;
	}
	
	
	
}
