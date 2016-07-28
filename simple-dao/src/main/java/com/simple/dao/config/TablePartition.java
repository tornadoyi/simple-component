/**
 * 
 */
package com.simple.dao.config;

import java.text.MessageFormat;

import com.simple.base.util.expression.Expression;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月27日
 */
public class TablePartition {
	private String tableName;
	private String keyColumn;
	private String partitionRule;
	private String tableNameFormat;
	private MessageFormat formatPattern;
	private Expression expression;
	
	
	
	public String getKeyColumn() {
		return keyColumn;
	}
	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
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
	public MessageFormat getFormatPattern() {
		return formatPattern;
	}
	public void setFormatPattern(MessageFormat formatPattern) {
		this.formatPattern = formatPattern;
	}
	public Expression getExpression() {
		return expression;
	}
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	
}
