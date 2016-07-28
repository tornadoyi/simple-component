/**
 * 
 */
package com.simple.dao.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.simple.dao.core.impl.DeleteOperation;
import com.simple.dao.core.impl.InsertOperation;
import com.simple.dao.core.impl.ReplaceOperation;
import com.simple.dao.core.impl.SelectOperation;
import com.simple.dao.core.impl.UpdateOperation;
import com.simple.dao.error.DaoException;

/**
 * SQL操作类型。
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public enum OperationType {
    INSERT(InsertOperation.class, false),
    UPDATE(UpdateOperation.class, false),
    DELETE(DeleteOperation.class, false),
//    SELECT(SelectOperation.class, true),
    //这里将唯一读操作改成了写，是为了不区分读写数据源，全部使用写数据源
    SELECT(SelectOperation.class, false),
    REPLACE(ReplaceOperation.class, false);


    private transient final Constructor<? extends SqlOperation> constrctor;
    private boolean isRead;

    private OperationType(Class<? extends SqlOperation> cls, boolean isRead) {
        this.isRead = isRead;

        try {
            this.constrctor = cls.getConstructor(new Class[]{Method.class, DaoProxy.class});
        } catch (Exception e) {
            throw new DaoException(
                    "Failed to get constructor from class " + cls.getName(), e);
        }
    }

    /**
     * 获得SQL操作对象的实例。
     * @param method 定义SQL操作的方法
     * @return 返回新的SQL对象实例
     */
    public SqlOperation getOperation(Method method, DaoProxy proxy) {
        try {
            return constrctor.newInstance(new Object[]{method, proxy});
        } catch (Exception e) {
            throw new DaoException(
                    "Failed to create new instance of class " + constrctor.getDeclaringClass().getName() , e);
        }
    }

    /**
     * 返回这种类型的操作是否是读操作。
     * @return 如果是读操作则返回true，否则返回false
     */
    public boolean isRead() {
        return isRead;
    }   
}
