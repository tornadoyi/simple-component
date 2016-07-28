/**
 * 
 */
package com.simple.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.simple.dao.bean.DBUnitInfo;
import com.simple.dao.config.DBConfig;
import com.simple.dao.config.DataSourceConfig;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月1日
 */
public class TestSimpleSqlExecuter {
	public static void main(String args[]){
		try {
			
			DBConfig dbConfig = new DBConfig() {
				
				@Override
				public List<DataSourceConfig> getDbSourceConfigList() {
					
					List<DataSourceConfig> dsList = new ArrayList<DataSourceConfig>();
					
					DataSourceConfig datasource = new DataSourceConfig();
					datasource.setName("DS1");
					
					List<DBUnitInfo> unitList = new ArrayList<DBUnitInfo>();
					DBUnitInfo unit = new DBUnitInfo();
					unit.setHost("10.130.137.64");
					unit.setPassword("1234qwer!");
					unit.setDbName("game_account");
					unit.setRead(true);
					unit.setWrite(true);
					unit.setUserName("root");
					unitList.add(unit);
					datasource.setUnitList(unitList);
					
					
					
					DataSourceConfig datasource2 = new DataSourceConfig();
					datasource2.setName("DS2");
					
					List<DBUnitInfo> unitList2 = new ArrayList<DBUnitInfo>();
					DBUnitInfo unit2 = new DBUnitInfo();
					unit2.setHost("10.130.137.64");
					unit2.setPassword("1234qwer!");
					unit2.setDbName("game_account");
					unit2.setRead(true);
					unit2.setWrite(true);
					unit2.setUserName("root");
					unitList2.add(unit2);
					datasource2.setUnitList(unitList2);
					
					dsList.add(datasource);
					dsList.add(datasource2);
					return dsList;
				}
			};
			
			DatasourceFactory.loadDataSourceConfig(dbConfig);
			
			
			List<SpiralAccountInfo> info  = SimpleSQLExecuter.queryList("ds1", SpiralAccountInfo.class, "SELECT * FROM game_account.spiral_account_info");
			System.out.println(JSON.toJSONString(info));
			
			
			SimpleSQLExecuter.update("ds1", "update spiral_account_info set pwd = ? where account_id = ?", "xxx", 2);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
