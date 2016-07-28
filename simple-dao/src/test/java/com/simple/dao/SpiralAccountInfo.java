/**
 * 
 */
package com.simple.dao;

import com.simple.dao.core.DaoModel;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月30日
 */
public class SpiralAccountInfo implements DaoModel{
	/**  **/
	private static final long serialVersionUID = 2572465561339909528L;
	private int accountId;
	private int accountType;
	private String phone;
	private String mail;
	private String pwd;
	private int platformType;
	private String platformAccount;
	private byte[] platformExtends;
	private String deviceId;
	private int registTime;
	
	
	public int getAccountType() {
		return accountType;
	}
	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}
	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public int getPlatformType() {
		return platformType;
	}
	public void setPlatformType(int platformType) {
		this.platformType = platformType;
	}
	public String getPlatformAccount() {
		return platformAccount;
	}
	public void setPlatformAccount(String platformAccount) {
		this.platformAccount = platformAccount;
	}
	
	
	public byte[] getPlatformExtends() {
		return platformExtends;
	}
	public void setPlatformExtends(byte[] platformExtends) {
		this.platformExtends = platformExtends;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public int getRegistTime() {
		return registTime;
	}
	public void setRegistTime(int registTime) {
		this.registTime = registTime;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	
}
