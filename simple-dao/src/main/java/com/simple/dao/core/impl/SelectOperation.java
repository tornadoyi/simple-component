package com.simple.dao.core.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.simple.dao.bean.SqlExecuteBean;
import com.simple.dao.bean.SqlExecuteBean.SqlExecutor;
import com.simple.dao.core.AbstractSqlOperation;
import com.simple.dao.core.DaoProxy;
import com.simple.dao.core.OperationType;


/**
 * 查询操作。
 * @since 1.0
 */
public class SelectOperation extends AbstractSqlOperation {

    public SelectOperation(Method method, DaoProxy proxy) {
        super(method, proxy);
    }

    @Override
    public OperationType getType() {
        return OperationType.SELECT;
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object parseResults(List<Object> rs) {
        if (List.class.isAssignableFrom(returnType)) {
            List<Object> results = new ArrayList<Object>();

            for (Object result : rs) {
                results.addAll((List<Object>) result);
            }

            return results;
        } else {
            return rs.get(0);
        }

    }

    @Override
    protected SqlExecutor getDefaultExecutor() {
        return SqlExecuteBean.QUERY;
    }

    @Override
    protected Map<String, List<SqlExecuteBean>> filterSqlBean(
            Map<String, List<SqlExecuteBean>> sqlMap) {
        return sqlMap;
    }
}
