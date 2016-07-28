/**
 * 
 */
package com.simple.dao;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.simple.base.config.XmlConfig;
import com.simple.base.logger.BaseLogger;
import com.simple.base.util.StringUtil;
import com.simple.base.util.expression.Expression;
import com.simple.base.util.expression.ExpressionUtil;
import com.simple.dao.bean.DBUnitInfo;
import com.simple.dao.bean.DbDataSource;
import com.simple.dao.config.DBConfig;
import com.simple.dao.config.DataSourceConfig;
import com.simple.dao.config.TableConfig;
import com.simple.dao.config.TablePartition;
import com.simple.dao.config.impl.simplexml.DBConfigXml;
import com.simple.dao.config.impl.simplexml.TableConfigXml;
import com.simple.dao.error.DaoException;
import com.simple.dao.logger.DaoLogger;

/**
 * 数据源工厂类，负责加载数据源的配置信息并对外提供数据库连接
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class DatasourceFactory {
	private static  Logger logger = DaoLogger.getDaoLogger();
	
    private static Map<DBUnitInfo, DataSource> datasourceCache = new ConcurrentHashMap<DBUnitInfo, DataSource>();
    
    /** 关闭数据源执行器 **/
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    /** 检查是否被关闭过 **/
    private static Map<DataSource, Integer> closingDataSource = new ConcurrentHashMap<DataSource, Integer>();
    
    /** 真实数据源缓存 **/
    private static Map<String, DbDataSource> dbDataSourceMap = new ConcurrentHashMap<String, DbDataSource>();
    
    
    /** 散表配置 **/
    private static Map<String, TablePartition> tablePartitionMap = new ConcurrentHashMap<String, TablePartition>();
    
	
    private static final int MAX_CONNECTION_PER_DATASOURCE = 1024;
    /** 数据源构造器 **/
    private static DataSourceBuilder builder = new C3p0DataSourceBuilder();
    
    static{
    	DBConfigXml dbXmlConfig = new DBConfigXml();
		loadDataSourceConfig(dbXmlConfig);
		
		TableConfigXml tableXmlConfig = new TableConfigXml();
		loadTableConfig(tableXmlConfig);
		
    }
    
    public synchronized static void loadDataSourceConfig(DBConfig dbConfig){
    	if(dbConfig.getDbSourceConfigList() == null || dbConfig.getDbSourceConfigList().size() == 0){
    		logger.error("load datasource config failed ");
    		return;
    	}
    	//关闭旧的数据源
    	logger.debug("start to load datasource config");
    	for(DbDataSource dbDataSource : dbDataSourceMap.values()){
    		dbDataSource.close();
    	}
    	dbDataSourceMap.clear();
    
    	for(DataSourceConfig config : dbConfig.getDbSourceConfigList()){
    		DbDataSource dbDataSource = new DbDataSource();
    		dbDataSource.setName(config.getName());
    		dbDataSource.setDatasourceUnits(config.getUnitList());
    		dbDataSourceMap.put(config.getName().toLowerCase(), dbDataSource);
    		logger.debug("load datasource : " + config.getName());
    	}
    	logger.debug("load datasource config success");
    	
    }
    
    public synchronized static void loadTableConfig(TableConfig tableConfig){
    	if(tableConfig.getTablePartitionList() == null || tableConfig.getTablePartitionList().size() == 0){
    		return;
    	}
    	//清空旧的配置
    	tablePartitionMap.clear();
    	
    	for(TablePartition partition : tableConfig.getTablePartitionList()){
    		if(partition.getTableNameFormat() == null || partition.getTableNameFormat().trim().equals("")){
    			logger.error(partition.getTableName() + "没有配置散表规则");
    			continue;
    		}
    		Expression expression = ExpressionUtil.buildExpression(partition.getPartitionRule());
    		partition.setExpression(expression);
    		partition.setFormatPattern(new MessageFormat(partition.getTableNameFormat()));
    		tablePartitionMap.put(partition.getTableName(), partition);
    		logger.debug("load table config:" + partition.getTableName());
    	}
    	logger.debug("load table config success");
    }
    
    /**
     * 获取散表配置
     * @param tableName
     * @return
     */
    public static TablePartition getTablePartition(String tableName){
    	return tablePartitionMap.get(tableName);
    }
    
    
    
    
    /**
     * 获取散表后的结果
     * @param tableName
     * @param key
     * @return
     */
    public static String getPartitionTableName(String tableName, Object param){
    	TablePartition partition = getTablePartition(tableName);
    	if(partition == null){
    		return tableName;
    	}
    	
    	Expression expression = partition.getExpression();
    	Long key;
    	if (param.getClass().equals(int.class)
                 || param.getClass().equals(Integer.class)
                 ||(param.getClass().equals(long.class))
                 || param.getClass().equals(Long.class)) {
    		key = Long.valueOf(param.toString());
         }else{
        	 logger.error("散表关键列必须为数字");
        	 return tableName;
         }
    	return partition.getFormatPattern().format(
                new Object[] {String.valueOf(expression.getValue(key))});
    }
    
    
    /**
     * 获得指定数据源类型的数据库连接。
     * @param dbType 指定数据源类型
     * @param isRead 是否是读连接
     * @return 返回获得的数据库连接
     * @throws SQLException
     */
    public static Connection getConnection(String dbType, boolean isRead) throws SQLException {
        return isRead ? getReadConnection(dbType) : getWriteConnection(dbType);
    }
    
    
    /**
     * 获得写连接。
     * @param dbSourceName 指定的数据源类型
     * @return 如果存在这种数据源，则返回其写连接
     * @throws SQLException 如果不存在这种数据源类型对应的数据源实例时，抛出此异常
     */
    public static Connection getWriteConnection(String dbSourceName) throws SQLException {
        DbDataSource dataSource = dbDataSourceMap.get(dbSourceName);

        if (null == dataSource) {
            throw new DaoException("名称为" + dbSourceName + "的数据源没有被配置。");
        }
        logger.debug("getReadConnection from ["+dbSourceName+"]");
        return dataSource.getWriteConnection();
    }
    
    
    /**
     * 获得读连接。
     * @param dbSourceName 指定的数据源类型
     * @return 如果存在这种数据源，则返回其读连接
     * @throws SQLException 如果不存在这种数据源类型对应的数据源实例时，抛出此异常
     */
    public static Connection getReadConnection(String dbSourceName) throws SQLException {
        DbDataSource dataSource = dbDataSourceMap.get(dbSourceName);
        if (null == dataSource) {
            throw new DaoException("名称为" + dbSourceName + "的数据源没有被配置。");
        }
        logger.debug("getReadConnection from ["+dbSourceName+"]");
        return dataSource.getReadConnection();
    }
    
    
    

    /**
     * 根据传入的配置信息构建数据源实例。
     * @param config 数据源配置信息
     * @return 返回构建好的数据源实例
     */
    public static DataSource buildDataSource(DBUnitInfo config) {
        DataSource dataSource = datasourceCache.get(config);

        if (null == dataSource) {
            dataSource = createRealDataSource(config);
            datasourceCache.put(config, dataSource);
        }
        return dataSource;
    }
    
    /**
     * 构造一个实际的数据源实例。
     * @param config 数据源的配置信息
     * @return 返回数据源实例
     */
    private static DataSource createRealDataSource(DBUnitInfo config) {
        return builder.buildDataSource(config);
    }
    
    /**
     * 关闭数据源。
     * @param dataSource 数据源实例
     */
    public static void closeDataSource(DataSource dataSource) {
        if (null == dataSource) {
            return;
        }

        if (dataSource instanceof ComboPooledDataSource) {
            if (closingDataSource.containsKey(dataSource)) {
                return;
            }
            closingDataSource.put(dataSource, 1);

            final ComboPooledDataSource pooledDataSource = (ComboPooledDataSource) dataSource;
            executor.execute(new Runnable() {
               public void run() {
                   boolean finish = false;
                   try {
                       int activeCount = 0;
                       while (!finish) {
                           activeCount = pooledDataSource.getNumBusyConnections();
                           if (activeCount == 0) {
                               finish = true;
                               logger.info("Closing data source " + pooledDataSource.getJdbcUrl()
                                       + ", user name " + pooledDataSource.getUser()
                                       + " password " + pooledDataSource.getPassword());
                               pooledDataSource.close();
                               closingDataSource.remove(pooledDataSource);
                           } else {
                               try {
                                   Thread.sleep(1000);
                               } catch (InterruptedException e) {
                                   //忽略
                               }
                           }
                       }
                   } catch (SQLException e) {
                	   logger.error("Failed to close data source."
                               + pooledDataSource.getJdbcUrl()
                               + ", user name " + pooledDataSource.getUser(), e);
                       throw new DaoException(e);
                   }
               }
            });
        }
    }
    
	
    /**
     * 数据源构造器，负责构造实际的数据源。
 
     * @version 1.0 2010-09-26 16:43:47
     * @since 1.0
     */
    public interface DataSourceBuilder {

        /**
         * 通过配置信息构造一个数据源实例。
         * @param config 数据源的相关配置信息
         * @return 返回数据源实例
         */
        public DataSource buildDataSource(DBUnitInfo config);
    }
    
    
    /**
     * C3p0数据源构造器，根据配置构造出c3p0的数据源(池)实例。
     * @since 1.0
     */
    private static class C3p0DataSourceBuilder implements DataSourceBuilder {

		/* (non-Javadoc)
		 * @see com.simple.dao.DatasourceFactory.DataSourceBuilder#buildDataSource(com.simple.dao.bean.DataSourceUnit)
		 */
		public DataSource buildDataSource(DBUnitInfo config) {
      
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            try {
                dataSource.setDriverClass("com.mysql.jdbc.Driver");
            } catch (PropertyVetoException e) {
                throw new DaoException("Unsupported jdbc driver. Cause by : " + e.getMessage());
            }
            dataSource.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDbName()+"?characterEncoding=utf8");
            dataSource.setUser(config.getUserName());
            dataSource.setPassword(config.getPassword());
            dataSource.setMaxPoolSize(Math.max(MAX_CONNECTION_PER_DATASOURCE, config.getMaxPoolSize()));
            dataSource.setIdleConnectionTestPeriod(60);
            dataSource.setPreferredTestQuery("select now()");
            dataSource.setMinPoolSize(100);
            dataSource.setInitialPoolSize(config.getInitPoolSize());
            dataSource.setAcquireIncrement(5);
            dataSource.setMaxIdleTime(60);
            dataSource.setTestConnectionOnCheckin(true);
            return dataSource;
	        
		}



    }
	
}
