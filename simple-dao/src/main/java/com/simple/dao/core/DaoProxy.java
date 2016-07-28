/**
 * 
 */
package com.simple.dao.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

import com.simple.dao.annotation.DataSource;
import com.simple.dao.error.DaoException;
import com.simple.dao.logger.DaoLogger;
import com.simple.dao.parser.SQLMethodParser;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class DaoProxy implements InvocationHandler {
	private static final Logger logger = DaoLogger.getDaoLogger();
    private final Map<Method, SqlOperation> operationMap;
	
	
	
	/** 默认的数据源 **/
	private final String defaultDataSource;
	
	public DaoProxy(Class<?> daoClass){
		DataSource dataSource = daoClass.getAnnotation(DataSource.class);
		if (dataSource == null) {
            throw new DaoException("Data source annotation has not been found from class " + daoClass.getName());
        }
		defaultDataSource = dataSource.value().toLowerCase();
		
		//取得各方法解析结果
        this.operationMap = SQLMethodParser.parseMethod(daoClass, this);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Create dao instance from class " + daoClass.getName()
                    + ", use default data source " + defaultDataSource);
        }
	}
	
	
	public DaoProxy(Class<?> daoClass, String datasourceName) {
		if (datasourceName == null) {
            throw new DaoException("Data source annotation has not been found from class "  + daoClass.getName());
        }
		defaultDataSource = datasourceName;
		 //取得各方法解析结果
        this.operationMap = SQLMethodParser.parseMethod(daoClass, this);
        
        
        if (logger.isDebugEnabled()) {
            logger.debug("Create dao instance from class " + daoClass.getName()
                    + ", use default data source " + defaultDataSource);
        }
	}
	
	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		//执行SQL
        SqlOperation operation = operationMap.get(method);

        return operation.execute(args, defaultDataSource);
	}


	public String getDefaultDataSource() {
		return defaultDataSource;
	}
	
	
	
	
	
	
}
