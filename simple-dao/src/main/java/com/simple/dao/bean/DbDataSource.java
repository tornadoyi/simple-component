/**
 * 
 */
package com.simple.dao.bean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import com.simple.dao.DatasourceFactory;
import com.simple.dao.error.DaoException;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class DbDataSource {
	private String name;
	
	private transient final Random random = new Random(System.currentTimeMillis());
	private List<DBUnitInfo> datasourceUnits = new ArrayList<DBUnitInfo>();
	private transient DataSource writeDataSource = null;
	private transient List<DataSource> readDataSources = null;
	
	private boolean init = false;
	
    public List<DBUnitInfo> getDatasourceUnits() {
        return datasourceUnits;
    }

    public void setDatasourceUnits(List<DBUnitInfo> datasourceUnits) {
        this.datasourceUnits = datasourceUnits;
    }
    
    
    public void setName(String name) {
		this.name = name;
	}

	/**
     * 从可以读的数据源中随机获得一个读连接。
     * @return 返回获得的读连接
     * @throws SQLException
     */
    public Connection getReadConnection() throws SQLException {
        if (!init) {
            init();
        }

        return readDataSources.get(random.nextInt(readDataSources.size())).getConnection();
    }
    
    /**
     * 从唯一的写数据源中获得一个写连接。
     * @return 返回写连接
     * @throws SQLException
     */
    public Connection getWriteConnection() throws SQLException {
        if (!init) {
            init();
        }

        return writeDataSource.getConnection();
    }
    
    
    public boolean close() {
        if (!init) {
            return true;
        }
        
        if (writeDataSource != null) {
            DatasourceFactory.closeDataSource(writeDataSource);
        }

        for (DataSource dataSource : readDataSources) {
            DatasourceFactory.closeDataSource(dataSource);
        }

        return true;
    }
    
    
    /**
     * 通过配置信息初始化数据源实例
     */
    synchronized void init() {
        if (init) {
            return;
        }
        
        for (DBUnitInfo unit : datasourceUnits) {
            if (unit.isWrite()) {
                if (writeDataSource != null) {
                    throw new DaoException("More than one write datasource has been set for " + name);
                }

                writeDataSource = DatasourceFactory.buildDataSource(unit);
            }

            if (unit.isRead()) {
                readDataSources = new ArrayList<DataSource>();
                readDataSources.add(DatasourceFactory.buildDataSource(unit));
            }
        }
        
        init = true;
    }


    
}
