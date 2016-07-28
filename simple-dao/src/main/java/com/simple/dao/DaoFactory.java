/**
 * 
 */
package com.simple.dao;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.simple.dao.config.DBConfig;
import com.simple.dao.core.DaoProxy;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public class DaoFactory {

	/** 类的数据源集合 **/
	private static final Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();
	
	/** 指定数据源的方法集合 **/
	private static final Map<String, Object> assignDataSourceMap = new HashMap<String, Object>();
	 
	 
	private static final ClassLoader clsLoader = DaoFactory.class.getClassLoader();
	
	/**
     * 获得指定接口类定义的DAO实例。
     * @param <T> 定义DAO的接口类
     * @param cls 接口类的class
     * @return 返回DAO的实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDao(Class<T> cls) {
        return (map.containsKey(cls)) ? (T)map.get(cls) : createDaoProxy(cls);
    }
    
    /**
     * 得指定接口类定义的DAO实例 并指定数据源
     * @param cls
     * @param datasourceName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDao(Class<T> cls, String datasourceName) {
    	 datasourceName = datasourceName.toLowerCase();
    	 return (assignDataSourceMap.containsKey(cls.getName() + datasourceName)) ? (T)assignDataSourceMap.get(cls.getName() + datasourceName) : createDaoProxyAssignDataSource(cls, datasourceName);
    }
    
    
    /**
     * 创建一个指定接口类的代理实例。
     * @param <T> 接口类型
     * @param cls 接口的class
     * @return 返回指定接口的代理实例
     */
    @SuppressWarnings("unchecked")
    private static <T> T createDaoProxy(Class<T> cls) {
        T proxy;
        synchronized(cls) {
            if (map.containsKey(cls)) return (T) map.get(cls);

            //创建DAO代理
            proxy = (T) Proxy.newProxyInstance(clsLoader, new Class[]{cls}, new DaoProxy(cls));

            map.put(cls, proxy);
        }
        return proxy;
    }
    
    
    /**
     * 创建一个指定接口类的代理实例。
     * @param <T> 接口类型
     * @param cls 接口的class
     * @return 返回指定接口的代理实例
     */
    @SuppressWarnings("unchecked")
    private static <T> T createDaoProxyAssignDataSource(Class<T> cls, String datasourceName) {
        T proxy;
        synchronized(cls) {
            if (assignDataSourceMap.containsKey(cls.getName() + datasourceName)) return (T) assignDataSourceMap.get(cls.getName() + datasourceName);

            //创建DAO代理
            proxy = (T) Proxy.newProxyInstance(clsLoader, new Class[]{cls}, new DaoProxy(cls, datasourceName));

            assignDataSourceMap.put(cls.getName() + datasourceName, proxy);
        }
        return proxy;
    }
    

}
