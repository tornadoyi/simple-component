/**
 * 
 */
package com.simple.dao.config;

import java.util.List;

import com.simple.dao.bean.DBUnitInfo;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class DataSourceConfig {
	private String name;
	private List<DBUnitInfo> unitList;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<DBUnitInfo> getUnitList() {
		return unitList;
	}
	public void setUnitList(List<DBUnitInfo> unitList) {
		this.unitList = unitList;
	}
	
	
}
