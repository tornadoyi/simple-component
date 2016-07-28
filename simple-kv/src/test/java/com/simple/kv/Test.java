/**
 * 
 */
package com.simple.kv;

import java.net.URL;

import org.apache.log4j.PropertyConfigurator;

import com.simple.base.util.tuple.Tuple;
import com.simple.kv.storage.HeStorageFactory;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月20日
 */
public class Test {
	public static void main(String args[]){
		PropertyConfigurator.configure(Test.class.getResource("log4j.properties"));
		UserInfoDao userInfoDao = HeStorageFactory.getDAO(UserInfoDao.class);
		UserInfo userInfo = new UserInfo();
		userInfo.setId(100L);
		userInfo.setName("test100");
		userInfo.setLevel(100);
//		
		userInfoDao.set(Tuple.tuple("test100", 100), userInfo);
		
		
		UserInfo userInfo2 = userInfoDao.get(Tuple.tuple("test100", 100));
		System.out.println(userInfo2.getName());
	}
}
