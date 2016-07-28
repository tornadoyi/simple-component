/**
 * 
 */
package com.simple.dao.bean;

import java.io.Serializable;

/**
 * 数据源配置
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class DBUnitInfo implements Serializable {

	/**  **/
	private static final long serialVersionUID = 1805749355772074558L;
	private String dbName;
    private String host;
    private int port = 3306;
    private String userName;
    private String password;
    
    private int initPoolSize;
    private int maxPoolSize;
    
    /** 可读 **/
    private boolean read;
    /** 可写 **/
    private boolean write;
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public boolean isWrite() {
		return write;
	}
	public void setWrite(boolean write) {
		this.write = write;
	}
	public int getInitPoolSize() {
		return initPoolSize;
	}
	public void setInitPoolSize(int initPoolSize) {
		this.initPoolSize = initPoolSize;
	}
	public int getMaxPoolSize() {
		return maxPoolSize;
	}
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}
    
    
    
    

}
