/**
 * 
 */
package com.simple.dao.core;

/**
 * SQL操作的接口类。
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public interface SqlOperation {
    /**
     * 获得当前SQL操作类型。
     * @see OperationType
     * @return 返回当前操作的实际类型
     */
    public OperationType getType();

    /**
     * 根据传入的实际参数以及默认的数据源，执行当前SQL操作，并返回结果
     * @param args 实际传入的参数
     * @param defaultDataSource 默认的数据源
     * @return 返回方法定义的返回值
     */
    public Object execute(Object[] args, String defaultDataSource);

}
