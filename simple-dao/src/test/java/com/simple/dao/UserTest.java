/**
 * 
 */
package com.simple.dao;

import com.simple.dao.core.DaoModel;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class UserTest implements DaoModel{
	/**  **/
	private static final long serialVersionUID = -1994837136234073506L;
	private int id;
	private String username;
	private int age;
	
	private byte [] testBlob;
	
	
	private String upTime;
	
	public UserTest() {
		super();
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getUpTime() {
		return upTime;
	}
	public void setUpTime(String upTime) {
		this.upTime = upTime;
	}
	public byte[] getTestBlob() {
		return testBlob;
	}
	public void setTestBlob(byte[] testBlob) {
		this.testBlob = testBlob;
	}
	
	
	
}
