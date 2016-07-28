package com.simple.dao.core.impl;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.simple.dao.DaoParamType;
import com.simple.dao.bean.ArgParam;
import com.simple.dao.bean.SqlExecuteBean;
import com.simple.dao.bean.SqlExecuteBean.SqlExecutor;
import com.simple.dao.core.AbstractSqlOperation;
import com.simple.dao.core.DaoProxy;
import com.simple.dao.core.OperationType;
import com.simple.dao.error.DaoException;
import com.simple.dao.parser.token.ExpressionToken;


/**
 * 插入操作。
 * @since 1.0
 */
public class InsertOperation extends AbstractSqlOperation {

    //获得最后插入的自增长id的sql
    private static final SqlExecuteBean GET_INSERT_ID;
    private ArgParam beanListParam = null;

    static {
        GET_INSERT_ID = new SqlExecuteBean();
        GET_INSERT_ID.appendSql("select last_insert_id()");
        GET_INSERT_ID.setExecutor(SqlExecuteBean.QUERY);
    }

    public InsertOperation(Method method, DaoProxy proxy) {
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

        validateReturnType();
    }

    @Override
    public OperationType getType() {
        return OperationType.INSERT;
    }

    @Override
    protected boolean validate() {
        return true;
    }

    private void validateReturnType() {
        //插入操作必须有参数
        if (argParams.isEmpty()) {
            throw new DaoException(
                    "Insert operation need params in method " + getMethodFullName());
        }

        //返回值必须是数值类型或者void
        if (null == beanListParam && !NUMBER_TYPE.contains(returnType) && !VOID_TYPE.contains(returnType)) {
            throw new DaoException(
                    "Invalid return type " + returnType.getName()
                    + " for insert operation (method: " + getMethodFullName() + ").");
        }

        /**
         * 批量处理时只能返回void
         */
        if (null != beanListParam && !VOID_TYPE.contains(returnType)) {
            throw new DaoException(
                    "批量插入操作的返回值只能是void(method: " + getMethodFullName() + ")");
        }
    }

    @Override
    protected Object parseResults(List<Object> rs) throws SQLException {

        //当返回值是数值类型时，从最后一个执行的sql结果中解析自增长id
        if (null == beanListParam && NUMBER_TYPE.contains(returnType)) {
            return rs.get(rs.size() - 1);
        }

        return null;
    }

    @Override
    protected SqlExecutor getDefaultExecutor() {
        return SqlExecuteBean.EXECUTE;
    }

    @Override
    protected Map<String, List<SqlExecuteBean>> filterSqlBean(
            Map<String, List<SqlExecuteBean>> sqlMap) {

        //如果返回是数值类型，则应该只有一个SQL语句，否则无法取到自增长id
        //如果是批量插入，则只有一个boolean的返回值，不能取到id
        if (!VOID_TYPE.contains(returnType) && null == beanListParam) {
            int sqlCount = 0;
            for (Map.Entry<String, List<SqlExecuteBean>> entry : sqlMap.entrySet()) {
                SqlExecuteBean bean = GET_INSERT_ID.getCopy();
                bean.setDbType(entry.getKey());
                entry.getValue().add(bean);

                sqlCount += entry.getValue().size();
            }

            //加上去自增id的sql，一共只能有2个sql语句存在
            if (sqlCount > 2) {
                throw new DaoException(
                        "Only single insert operation can return auto increment primary key."
                        + "Method: " + getMethodFullName() + "'s parameter is invalid. Real sql count is " + sqlCount);
            }
        }

        //如果是bean的集合参数，则需要重新组装sql语句以及参数
        if (null != beanListParam) {
            sqlMap = addListBeanParam(beanListParam, sqlMap);
        }

        return sqlMap;
    }

}
