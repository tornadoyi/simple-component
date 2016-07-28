/**
 * 
 */
package com.simple.base.xmltest;

import org.simpleframework.xml.Attribute;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月21日
 */
public class Server {
	@Attribute(name = "database")
	private String database;
	@Attribute(name = "host")
	private String host;
	@Attribute(name = "port")
	private int port;
	@Attribute(name = "wrflag")
	private String wrFlag;
	@Attribute(name = "user")
	private String user;
	@Attribute(name = "password")
	private String password;
	@Attribute(name = "initPoolSize")
	private int initPoolSize;
	@Attribute(name = "maxPoolSize")
	private int maxPoolSize;
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
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
	public String getWrFlag() {
		return wrFlag;
	}
	public void setWrFlag(String wrFlag) {
		this.wrFlag = wrFlag;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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
