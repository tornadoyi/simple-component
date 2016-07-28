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
 * 更新操作。
 * @since 1.0
 */
public class UpdateOperation extends AbstractSqlOperation {

    public UpdateOperation(Method method, DaoProxy proxy) {
        super(method, proxy);
    }

    @Override
    public OperationType getType() {
        return OperationType.UPDATE;
    }

    @Override
    protected boolean validate() {
        if (!VOID_TYPE.contains(returnType) && !NUMBER_TYPE.contains(returnType)) {
            throw new DaoException("Invalid return type for update method " + getMethodFullName());
        }

        return true;
    }

    @Override
    public Object parseResults(List<Object> rs) {
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
