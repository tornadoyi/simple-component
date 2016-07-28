/**
 * 
 */
package com.simple.kv;

import com.simple.kv.storage.KeyParamAware;
import com.simple.kv.storage.serialization.HeSerializable;
import com.simple.kv.storage.serialization.HeSerializableType;
import com.simple.kv.storage.serialization.anno.HeSerializableField;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月20日
 */
public class UserInfo implements HeSerializable, KeyParamAware<Long> {
	 private long id;
	 
	  @HeSerializableField(key = 1)
	  private String name;
	 
	  @HeSerializableField(key = 2, type = HeSerializableType.UNSIGNED_MEDIUM_INT)
	  private int level;
	 
	  @Override
	  public Long readKeyParam() {
	        return id;
	  }
	  @Override
	  public void writeKeyParam(Long keyParam) {
	        this.id = keyParam;
	  }
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	  
	  
	  
	  
	  
}
