package com.simple.dao.core.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.simple.dao.DaoParamType;
import com.simple.dao.bean.ArgParam;
import com.simple.dao.bean.SqlExecuteBean;
import com.simple.dao.core.DaoProxy;
import com.simple.dao.core.OperationType;
import com.simple.dao.error.DaoException;
import com.simple.dao.parser.token.ExpressionToken;

/**
 * @since 1.0
 */
public class ReplaceOperation extends UpdateOperation {

    private ArgParam beanListParam = null;

    public ReplaceOperation(Method method, DaoProxy proxy) {
        super(method, proxy);
        for (ExpressionToken expression : expressions) {
            if (expression.getArg().getParamType() == DaoParamType.LIST_PARAM_BEAN) {
                if (method.getParameterTypes().length != 1) {
                    throw new DaoException(
                            "Bean list parameter must be the only one argument for method " + getMethodFullName());
                } else {
                    this.beanListParam = expression.getArg();
                }
            }
        }
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) return false;

        //replace操作必须有参数
        if (argParams.isEmpty()) {
            throw new DaoException(
                    "Insert operation need params in method " + getMethodFullName());
        }

        if (null != beanListParam && NUMBER_TYPE.contains(returnType)) {
            throw new DaoException(
                    "批量Replace操作的返回值只能是void。(method: " + getMethodFullName() + ")");
        }

        return true;
    }

    @Override
    public OperationType getType() {
        return OperationType.REPLACE;
    }

    @Override
    protected Map<String, List<SqlExecuteBean>> filterSqlBean(
            Map<String, List<SqlExecuteBean>> sqlMap) {
        //如果是bean的集合参数，则需要重新组装sql语句以及参数
        if (null != beanListParam) {
            return addListBeanParam(beanListParam, sqlMap);
        }

        return sqlMap;
    }

}
