/**
 * 
 */
package com.simple.dao;

import java.util.Random;

import org.apache.log4j.PropertyConfigurator;

import com.simple.base.config.XmlConfig;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月27日
 */
public class ThreadTest {
	public static void main(String args[]){
		System.setProperty(XmlConfig.XML_CONFIG_DIR_PATH_KAY, "/simple-dao/src/test/java");
		PropertyConfigurator.configure(Test.class.getResource("log4j.properties"));
		final TestDao testDao = DaoFactory.getDao(TestDao.class);
		final Random rand = new Random();
		testDao.getUserTestById(10, 1);
		
//		try {
//			Thread.sleep(200000L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		for(int i= 0; i< 100; i++){
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					for(int i=0;i < 1000;i++){
						
						testDao.insert(Thread.currentThread().getName(), rand.nextInt(100));
						
					}
					
				}
			});
			
			t.start();
		}
		
		
		try {
		Thread.sleep(20000000L);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}
}
