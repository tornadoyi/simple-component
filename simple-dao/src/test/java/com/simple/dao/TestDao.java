/**
 * 
 */
package com.simple.dao;

import java.util.List;

import com.simple.dao.annotation.Param;
import com.simple.dao.annotation.DataSource;
import com.simple.dao.annotation.SQL;
import com.simple.dao.DaoParamType;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
@DataSource("game_account")
public interface TestDao {
	//无条件的查询语句
	@SQL("select * from test1 where id > 0")
	public List<UserTest> getUserTestList();
	
	//有条件的查询语句
	@SQL("select * from test1 where id = :id and age = :age")
	public UserTest getUserTestById(@Param(code = "age") int age, @Param(code = "id") int id);
	
	@SQL("update test set age = :age where id = :id")
	public void updateUser(@Param(code = "age") int age, @Param(code = "id") int id);
	
	@SQL("update test1 set test_blob = :testBlob where id = :id")
	public void updateUserBlob(@Param(type = DaoParamType.PARAM_BEAN) UserTest userTest);
	
	
	//带返回值的插入语句
	@SQL("insert into test1(username,age) values(:username,:age)")
	public int insert(@Param(code = "username") String username, @Param(code = "age") int age);
	
	@SQL("insert into test1(username,age) values(:username,:age)")
	public int insert(@Param(type = DaoParamType.PARAM_BEAN) UserTest userTest);
	
	@SQL("select * from test1 where id in ($_ids)")
	public List<UserTest> getUserTestListByIds(@Param(code = "$_ids") List<Integer> ids);
	
//	@SQL("select test1.* from test1 right join test2 on test1.id = test2.id where test1.id > 0")
//	public List<UserTest> getUserTestListByJoin();
	
	
	
}
