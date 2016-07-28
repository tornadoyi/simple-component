package com.simple.dao.core.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.simple.dao.bean.SqlExecuteBean;
import com.simple.dao.bean.SqlExecuteBean.SqlExecutor;
import com.simple.dao.core.AbstractSqlOperation;
import com.simple.dao.core.DaoProxy;
import com.simple.dao.core.OperationType;
import com.simple.dao.error.DaoException;


/**
 * 删除操作。
 *
 * @since 1.0
 */
public class DeleteOperation extends AbstractSqlOperation {

    public DeleteOperation(Method method, DaoProxy proxy) {
        super(method, proxy);
    }

    @Override
    public OperationType getType() {
        return OperationType.DELETE;
    }

    @Override
    protected boolean validate() {
        if (!VOID_TYPE.contains(returnType) && !NUMBER_TYPE.contains(returnType)) {
            throw new DaoException("Invalid delete method " + getMethodFullName());
        }

        return true;
    }

    @Override
    protected Object parseResults(List<Object> rs) {
        if (VOID_TYPE.contains(returnType)) {
            return null;
        } else if (NUMBER_TYPE.contains(returnType)) {
            int sum = 0;
            for (Object result : rs) {
                sum += (Integer) result;
            }

            return sum;
        }

        return null;
    }

    @Override
    protected SqlExecutor getDefaultExecutor() {
        return SqlExecuteBean.UPDATE;
    }

    @Override
    protected Map<String, List<SqlExecuteBean>> filterSqlBean(
            Map<String, List<SqlExecuteBean>> sqlMap) {
        return sqlMap;
    }
}
