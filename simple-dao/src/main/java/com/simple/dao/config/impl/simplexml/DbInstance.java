/**
 * 
 */
package com.simple.dao.config.impl.simplexml;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import com.simple.base.util.CollectionUtil;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月21日
 */
public class DbInstance {
	@Attribute(name = "name")
	private String name;
	
	@ElementList(entry ="server", inline = true)
	private List<Server> serverList = CollectionUtil.newArrayList();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Server> getServerList() {
		return serverList;
	}
	public void setServerList(List<Server> serverList) {
		this.serverList = serverList;
	}
	
	
	
}
