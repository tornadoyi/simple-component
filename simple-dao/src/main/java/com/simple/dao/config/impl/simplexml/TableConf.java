/**
 * 
 */
package com.simple.dao.config.impl.simplexml;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.simple.base.util.CollectionUtil;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月26日
 */
@Root(name = "table_conf")
public class TableConf {
	@ElementList(name="partitions", entry="table", inline = false)
	private List<PartitionInfo> partitionInfoList = CollectionUtil.newArrayList();

	public List<PartitionInfo> getPartitionInfoList() {
		return partitionInfoList;
	}

	public void setPartitionInfoList(List<PartitionInfo> partitionInfoList) {
		this.partitionInfoList = partitionInfoList;
	}
	
	
	
}
