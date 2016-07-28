/**
 * 
 */
package com.simple.dao.parser.token;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月27日
 */
public class TableJoinToken extends AbstractSqlToken {
	private String table;
	private String column;
	public TableJoinToken(String table, String column) {
		super();
		this.table = table;
		this.column = column;
	}
	@Override
	public TokenType getType() {
		return TokenType.TABLE_JOIN;
	}
	@Override
	public String getContent() {
		return table + "." + column; 
	}
	
	
	
	public String getTable() {
		return table;
	}
	public String getColumn() {
		return column;
	}
	@Override
	public String toString() {
		 return "TableJoin token " + getContent();
	}
	
	
	
}
