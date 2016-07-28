/**
 * 
 */
package com.simple.dao.config.impl.simplexml;

import java.util.List;
import java.util.Map;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import com.simple.base.util.CollectionUtil;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月21日
 */
@Root(name = "db_conf")
public class DbConf {
	@ElementList(entry ="instance", inline = true)
	private List<DbInstance> instanceList = CollectionUtil.newArrayList();
	
	@ElementMap(name = "map_test_str",entry = "k", attribute= true, key = "name")
	private Map<Integer, String> testMap = CollectionUtil.newHashMap();

	public List<DbInstance> getInstanceList() {
		return instanceList;
	}

	public void setInstanceList(List<DbInstance> instanceList) {
		this.instanceList = instanceList;
	}
	
	@Commit
	public void doAfter(){
		System.out.println("do something  once the deserialization completes ");
	}
	

	
}
