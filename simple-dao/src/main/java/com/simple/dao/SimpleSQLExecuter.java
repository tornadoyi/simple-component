/**
 * 
 */
package com.simple.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.simple.dao.error.DaoException;
import com.simple.dao.logger.DaoLogger;
import com.simple.dao.parser.ResultSetParser;


/**
 * 原生sql执行器，用来执行DaoFactory不支持的操作，如联表（有散表情况的查询），其它情况不建议使用
 * @author yongchao.zhao@happyelements.com
 * 2016年7月1日
 */
public class SimpleSQLExecuter {
	
	private static Logger logger = DaoLogger.getDaoLogger();
	
	

	@SuppressWarnings("unchecked")
	public static <T> T queryUniq(String dbSourceName, Class<T> t, String sql, Object... params) throws SQLException {
		
		Connection conn = DatasourceFactory.getReadConnection(dbSourceName);
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// 所有的SQL语句都必须处理成preparedStatement
			stmt = conn.prepareStatement(sql);
			Object param = null;
			if (null != params) {
				for (int i = 0; i < params.length; i++) {
					param = params[i];
					if (null == param) {
						stmt.setNull(i + 1, Types.NULL);
					} else if (param instanceof Date) {
						stmt.setTimestamp(i + 1, new java.sql.Timestamp(
								((Date) param).getTime()));
					} else {
						stmt.setObject(i + 1, param);
					}
				}
			}
			// 执行SQL
			if (logger.isDebugEnabled()) {
				logger.debug("Execute Sql: autoCommit = "
						+ conn.getAutoCommit()
						+ " || sql = "
						+ sql
						+ " || params = "
						+ (params == null ? null : Arrays.toString(params)+" || datasource = " + dbSourceName ));
			}
			rs = stmt.executeQuery();
			return (T)ResultSetParser.parseRs(rs, t, t);
		}catch(Exception e){
			conn.close();
			throw new DaoException(sql, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> queryList(String dbSourceName, Class<T> t, String sql, Object... params) throws SQLException{
		
		Connection conn = DatasourceFactory.getReadConnection(dbSourceName);
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// 所有的SQL语句都必须处理成preparedStatement
			stmt = conn.prepareStatement(sql);
			Object param = null;
			if (null != params) {
				for (int i = 0; i < params.length; i++) {
					param = params[i];
					if (null == param) {
						stmt.setNull(i + 1, Types.NULL);
					} else if (param instanceof Date) {
						stmt.setTimestamp(i + 1, new java.sql.Timestamp(
								((Date) param).getTime()));
					} else {
						stmt.setObject(i + 1, param);
					}
				}
			}
			// 执行SQL
			if (logger.isDebugEnabled()) {
				logger.debug("Execute Sql: autoCommit = "
						+ conn.getAutoCommit()
						+ " || sql = "
						+ sql
						+ " || params = "
						+ (params == null ? null : Arrays.toString(params)+" || datasource = " + dbSourceName ));
			}
			rs = stmt.executeQuery();
			return (List<T>)ResultSetParser.parseRs(rs, List.class, t);
		}catch(Exception e){
			conn.close();
			throw new DaoException(sql, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}
			}
		}
	}
	
	
	public static int update(String dbSourceName, String sql, Object...params) throws SQLException{
		Connection conn = DatasourceFactory.getWriteConnection(dbSourceName);
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// 所有的SQL语句都必须处理成preparedStatement
			stmt = conn.prepareStatement(sql);
			Object param = null;
			if (null != params) {
				for (int i = 0; i < params.length; i++) {
					param = params[i];
					if (null == param) {
						stmt.setNull(i + 1, Types.NULL);
					} else if (param instanceof Date) {
						stmt.setTimestamp(i + 1, new java.sql.Timestamp(
								((Date) param).getTime()));
					} else {
						stmt.setObject(i + 1, param);
					}
				}
			}
			// 执行SQL
			if (logger.isDebugEnabled()) {
				logger.debug("Execute Sql: autoCommit = "
						+ conn.getAutoCommit()
						+ " || sql = "
						+ sql
						+ " || params = "
						+ (params == null ? null : Arrays.toString(params)+" || datasource = " + dbSourceName ));
			}
			return stmt.executeUpdate();
		}catch(Exception e){
			conn.close();
			throw new DaoException(sql, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}
			}
		}

	}
	
	
}
