/**
 * 
 */
package com.simple.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSON;
import com.simple.base.config.XmlConfig;
import com.simple.dao.config.TableConfig;
import com.simple.dao.config.TablePartition;
import com.simple.dao.config.impl.simplexml.TableConf;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(Test.class.getResource("log4j.properties"));
		
		System.setProperty(XmlConfig.XML_CONFIG_DIR_PATH_KAY, "/simple-dao/src/test/java");
		
		TableConf conf = XmlConfig.getConfig(TableConf.class);
		System.out.println(JSON.toJSONString(conf));
		
/*		
		TableConfig tableConfig = new TableConfig(){
			@Override
			public List<TablePartition> getTablePartitionList() {
				TablePartition partition = new TablePartition();
				partition.setKeyColumn("id");
				partition.setPartitionRule("$ % 2");
				partition.setTableName("test");
				partition.setTableNameFormat("test{0}");
				List<TablePartition> list = new ArrayList<TablePartition>();
				list.add(partition);
				return list;
			}
		};
		
		DatasourceFactory.loadTableConfig(tableConfig);
		
*/
/*		for(int i =0 ;i < 30;i++){
			TestDao testDao = DaoFactory.getDao(TestDao.class);
			//System.out.println(testDao.getUserTestList());
			UserTest test  = testDao.getUserTestById(1);
			System.out.println(JSON.toJSONString(test));
		}
		
		try {
			Thread.sleep(600000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
*/		
		TestDao testDao = DaoFactory.getDao(TestDao.class);
		UserTest test = new  UserTest();
		test.setId(551753);
		test.setTestBlob("Hello World".getBytes());
		testDao.updateUserBlob(test);
		
		
		UserTest test2 = testDao.getUserTestById(88, 551753);
		System.out.println(new String(test.getTestBlob()));
		
		
//		UserTest test = new  UserTest();
//		test.setAge(15);
//		test.setUsername("xxxxx");
//		int id = testDao.insert(test);
//		System.out.println(id);
		
/*		List<Integer> ids = new ArrayList<Integer>();
		ids.add(2);
		ids.add(3);
		ids.add(4);
		List<UserTest> list = testDao.getUserTestListByIds(ids);
		System.out.println(list.size());
*/		
		
//		String test = "test1.id";
//		System.out.println(test.split("\\.").length);
		
		/**
		 * 联接查询暂时不支持散表情况
		 */
	//	List<UserTest> list = testDao.getUserTestListByJoin();
	//	System.out.println(list.size());
//		
		
	}

}
