package com.simple.dao.bean;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * @since 1.0
 */
public class SqlExecuteBean {

    public static final SqlExecutor EXECUTE = new SqlExecutor() {
        @Override
        public Object execute(PreparedStatement stmt) throws SQLException {
            return stmt.execute();
        }
    };

    public static final SqlExecutor UPDATE = new SqlExecutor() {
        @Override
        public Object execute(PreparedStatement stmt) throws SQLException {
            return stmt.executeUpdate();
        }
    };

    public static final SqlExecutor QUERY = new SqlExecutor() {
        @Override
        public Object execute(PreparedStatement stmt) throws SQLException {
            return stmt.executeQuery();
        }
    };

    private StringBuilder sql = new StringBuilder();
    private String dbType = null;
    private List<Object> params = new ArrayList<Object>();
    private List<Object> args = new ArrayList<Object>();
    private SqlExecutor executor = null;

    public SqlExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(SqlExecutor executor) {
        this.executor = executor;
    }

    public String getSql() {
        return sql.toString();
    }

    public void appendSql(String sql) {
        this.sql.append(" ").append(sql);
    }

    public void setSql(String sql) {
        this.sql = new StringBuilder(sql);
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }

    /**
     * 获得一个当前实例的拷贝。
     * @return 返回一个复制的实例
     */
    public SqlExecuteBean getCopy() {
        SqlExecuteBean cloneBean = new SqlExecuteBean();
        cloneBean.dbType = this.dbType;
        cloneBean.args.addAll(this.args);
        cloneBean.params.addAll(this.params);
        cloneBean.sql.append(this.sql);
        cloneBean.executor = this.executor;

        return cloneBean;
    }

    /**
     * SQL执行器接口，负责以某种方式执行传入的PreparedStatement。
 
     * @version 1.0 2009-11-04 16:59:22
     * @since 1.0
     */
    public static interface SqlExecutor {
        public Object execute(PreparedStatement stmt) throws SQLException;
    }

}
