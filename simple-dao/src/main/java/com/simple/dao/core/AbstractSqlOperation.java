/**
 * 
 */
package com.simple.dao.core;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.simple.base.parser.ClassHelper;
import com.simple.dao.DaoParamType;
import com.simple.dao.DatasourceFactory;
import com.simple.dao.annotation.SQL;
import com.simple.dao.bean.ArgParam;
import com.simple.dao.bean.SqlExecuteBean;
import com.simple.dao.bean.SqlExecuteBean.SqlExecutor;
import com.simple.dao.config.TablePartition;
import com.simple.dao.error.DaoException;
import com.simple.dao.logger.DaoLogger;
import com.simple.dao.parser.ResultSetParser;
import com.simple.dao.parser.SQLMethodParser;
import com.simple.dao.parser.SimpleSqlParser;
import com.simple.dao.parser.token.ExpressionToken;
import com.simple.dao.parser.token.InClauseToken;
import com.simple.dao.parser.token.SqlToken;
import com.simple.dao.parser.token.StaticToken;
import com.simple.dao.parser.token.TableJoinToken;
import com.simple.dao.parser.token.TableToken;

/**
 * SQL操作的抽象基类，完成大部分SQL操作的工作。
 * @author yongchao.zhao@happyelements.com
 * 2016年6月23日
 */
public abstract class AbstractSqlOperation implements SqlOperation {

	private static Logger logger = DaoLogger.getDaoLogger();
	
	protected static final List<Class<?>> NUMBER_TYPE = new ArrayList<Class<?>>();
	protected static final List<Class<?>> VOID_TYPE = new ArrayList<Class<?>>();

	static {
		NUMBER_TYPE.add(int.class);
		NUMBER_TYPE.add(Integer.class);
		NUMBER_TYPE.add(long.class);
		NUMBER_TYPE.add(Long.class);
		VOID_TYPE.add(Void.class);
		VOID_TYPE.add(void.class);
	}
	
	protected final DaoProxy proxy;
	protected final Method method;
	protected String initialSql;

	// 参数map
	protected Map<String, ArgParam> argParams = new HashMap<String, ArgParam>();
	// sql语句解析出来的tokens
	protected List<SqlToken> sqlTokens = new ArrayList<SqlToken>();
	protected List<TableToken> tables = new ArrayList<TableToken>();
	protected List<ExpressionToken> expressions = new ArrayList<ExpressionToken>();
	protected List<InClauseToken> inClauses = new ArrayList<InClauseToken>();

	protected Class<?> returnType;
	protected Class<?> rawReturnType;
	
	
	public AbstractSqlOperation(Method method, DaoProxy proxy) {
		this.proxy = proxy;
		this.method = method;
		init();
		validate();
	}
	
	/**
	 * 初始化SQL操作类，解析方法以及相应的参数。
	 */
	protected void init() {
		this.argParams = SQLMethodParser.getArgParams(method);
		parseReturnType();
		parseSql();
	}
	
	/**
	 * 解析方法的返回值。
	 */
	protected void parseReturnType() {
		this.returnType = method.getReturnType();

		if (returnType.isArray()) {
			this.rawReturnType = returnType.getComponentType();
		} else if (List.class.isAssignableFrom(returnType)) {
			this.rawReturnType = ClassHelper.getParameterizedType(method.getGenericReturnType());
		}
	}
	
	/**
	 * 解析方法注解中的SQL语句。
	 */
	protected void parseSql() {
		String defaultDbType = this.proxy.getDefaultDataSource();

		SQL annoSql = this.method.getAnnotation(SQL.class);
		this.initialSql = annoSql.value();
		this.sqlTokens = SimpleSqlParser.parserSql(annoSql.value(),
				this.getType());

		// 检查并关联SQL中的参数与方法参数
		for (SqlToken token : sqlTokens) {
			switch (token.getType()) {
			case TABLE:
				TableToken tableToken = (TableToken) token;

//				TableConfig config = TableManager.getTableConfig(
//						tableToken.getContent(), defaultDbType);
//				// 如果配置了散表信息，则必须在方法参数中指定关键列
//				if (config != null
//						&& this.argParams.get(config.getKeyColumn()) == null) {
//					throw new GameInvalidLogicException("表（"
//							+ tableToken.getContent() + "）的散表关键列（"
//							+ config.getKeyColumn() + "）没有被作为方法参数传入。["
//							+ getMethodFullName() + "]");
//				}
				tables.add(tableToken);
				break;
			case EXPRESSIOIN:
				ExpressionToken expression = (ExpressionToken) token;
				ArgParam argParam = argParams.get(expression.getParamName());

				if (null == argParam) {
					logger.error("Param "
							+ expression.getContent()
							+ " has not been annotationed in method "
							+ getMethodFullName());
					throw new DaoException("Param "
							+ expression.getContent()
							+ " has not been annotationed in method "
							+ getMethodFullName());
				}

				expression.setArg(argParam);
				expressions.add(expression);
				break;
			case IN_CLAUSE:
				InClauseToken inClauseToken = (InClauseToken) token;
				argParam = argParams.get(inClauseToken.getContent());

				if (null == argParam) {
					throw new DaoException("Param "
							+ inClauseToken.getContent()
							+ " has not been annotationed in method "
							+ getMethodFullName());
				}

				inClauseToken.setArg(argParam);
				inClauses.add(inClauseToken);
				break;
			}
		}
	}
	
	
	/**
	 * 获得方法的全名，形式如： “class#method”
	 * 
	 * @return 返回方法的全名
	 */
	protected String getMethodFullName() {
		return method.getDeclaringClass().getName() + "#" + method.getName();
	}

	/**
	 * 根据bean集合的数值重新组装需要的sql。
	 * 
	 * @param beanListParam
	 *            bean集合参数对象
	 * @param sqlMap
	 *            已经处理好了的SQL集合
	 * @return 返回组装后的SQL集合
	 */
	protected Map<String, List<SqlExecuteBean>> addListBeanParam(
			ArgParam beanListParam, Map<String, List<SqlExecuteBean>> sqlMap) {
		for (List<SqlExecuteBean> beans : sqlMap.values()) {
			for (SqlExecuteBean bean : beans) {
				// 当前的sql语句已经包含了一组参数，例如 insert into user_info values (?, ?, ?)
				String initialSql = bean.getSql();
				// bean集合参数时，只允许一个方法参数
				List<Object> args = beanListParam.getListValue(bean.getArgs()
						.get(beanListParam.getArgIndex()));

				List<Object> params = new ArrayList<Object>();
				// 如果实际参数只有1个bean，则不需要修改sql语句
				if (args.size() > 1) {
					int index = initialSql.indexOf("values");
					if (index == -1) {
						throw new DaoException("Invalid sql "
								+ this.initialSql + " in method "
								+ getMethodFullName());
					}

					index += 6;
					// 取到sql语句中取掉参数的前半部分
					bean.setSql(initialSql.substring(0, index));

					// 取到参数部分的sql语句，并按照实际参数数量来重复参数部分的次数
					StringBuilder sqlParam = new StringBuilder();
					String paramPart = initialSql.substring(index);
					// int paramEnd = paramPart.indexOf(")");
					int paramEnd = findParamEndIndex(paramPart);
					// GameTracker.trace("Param part string is " + paramPart);
					// GameTracker.trace("Index of parethense is " + paramEnd);
					String paramString = paramPart.substring(0, paramEnd + 1);
					// GameTracker.trace("Param string is " + paramString);

					for (int i = 0; i < args.size(); i++) {
						sqlParam.append(paramString).append(",");
					}
					sqlParam.deleteCharAt(sqlParam.length() - 1);

					bean.appendSql(sqlParam.toString());
					bean.appendSql(paramPart.substring(paramEnd + 1));
				}

				// 循环设置实际的参数值
				for (Object arg : args) {
					for (ExpressionToken expression : expressions) {
						params.add(expression.getArg().getValue(arg));
					}
				}

				bean.setParams(params);
			}
		}

		return sqlMap;
	}
	
	private int findParamEndIndex(String paramPartSql) {
		int count = 0;
		char[] chars = paramPartSql.toCharArray();
		char c;

		for (int i = 0; i < chars.length; i++) {
			c = chars[i];
			if (c == '(') {
				count++;
			} else if (c == ')') {
				if (count == 1) {
					return i;
				} else {
					count--;
				}
			}
		}

		return -1;
	}
	
	/**
	 * 验证方法的定义以及SQL的语句是否符合操作类型的要求。
	 * 
	 * @return 如果符合要求则返回true，否则返回false
	 */
	protected abstract boolean validate();
	
	
	/**
	 * 获得当前SQL操作的默认的executor.
	 * 
	 * @see SqlExecutor
	 * @return 返回当前操作类型的默认的执行方法
	 */
	protected abstract SqlExecutor getDefaultExecutor();
	
	
	/**
	 * 根据实际参数组装好了新的SQL语句以及相应参数后将调用此方法， 此方法可以对处理出来的SQL语句做过滤或者添加其他的SQL等操作。
	 * 此方法的结果将被直接执行。
	 * 
	 * @param sqlMap
	 *            根据实际参数处理出来的SQL操作
	 * @return 返回最终的SQL集合
	 */
	protected abstract Map<String, List<SqlExecuteBean>> filterSqlBean(
			Map<String, List<SqlExecuteBean>> sqlMap);
	
	/**
	 * 对结果集做最后的处理，并返回方法的返回值。
	 * 
	 * @param rs
	 *            所有的sql操作结果按顺序保留在此集合中
	 * @return 返回方法的返回值
	 * @throws SQLException
	 */
	protected abstract Object parseResults(List<Object> rs) throws Exception;	
	
	/* (non-Javadoc)
	 * @see com.simple.dao.core.SqlOperation#execute(java.lang.Object[], java.lang.String)
	 */
	public Object execute(Object[] args, String defaultDataSource) {
		Map<String, List<SqlExecuteBean>> sqlExecuteMap = prepareSqlAndParams(
				args, defaultDataSource);

		Object result = null;
		Connection conn = null;
		try {
			List<Object> results = new ArrayList<Object>();
			for (Map.Entry<String, List<SqlExecuteBean>> entry : sqlExecuteMap
					.entrySet()) {
				// 取得数据库连接
			
				// 创建连接状态
				conn = DatasourceFactory.getConnection(entry.getKey(), getType().isRead());

				for (SqlExecuteBean bean : entry.getValue()) {
					results.add(executeBean(conn, bean));
				}
			}
			result = parseResults(results);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				System.out.println("conn.close");
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
		return null;
	}
	/**
	 * 执行一条SQL语句。
	 * 
	 * @param conn
	 *            数据库连接
	 * @param bean
	 *            要被指定的SQL bean对象
	 * @return 返回执行结果
	 * @throws SQLException 
	 */
	protected Object executeBean(Connection conn, SqlExecuteBean bean) throws SQLException {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		try {
			// 所有的SQL语句都必须处理成preparedStatement
			stmt = conn.prepareStatement(bean.getSql());
			
			// 设置SQL参数
			List<Object> params = bean.getParams();
			Object param = null;

			if (null != params) {
				for (int i = 0; i < params.size(); i++) {
					param = params.get(i);
					if (null == param) {
						stmt.setNull(i + 1, Types.NULL);
					} else if (param instanceof Date) {
						stmt.setTimestamp(i + 1, new java.sql.Timestamp(
								((Date) param).getTime()));
					} else {
						stmt.setObject(i + 1, param);
					}
				}
			}
			// 执行SQL
			if (logger.isDebugEnabled()) {
				logger.debug("Execute Sql: autoCommit = "
						+ conn.getAutoCommit()
						+ " || sql = "
						+ bean.getSql()
						+ " || params = "
						+ (params == null ? null : Arrays.toString(params
								.toArray())+" || datasource = " + bean.getDbType() ));
			}
			Object result = bean.getExecutor().execute(stmt);
			if (result instanceof ResultSet) {
				rs = (ResultSet) result;
				return ResultSetParser.parseRs(rs, this.returnType,
						this.rawReturnType);
			}

			return result;
		}catch(Exception e){
			conn.close();
			throw new DaoException(bean.getSql(), e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}
			}
		}
	}

	
	
	
	/**
	 * 根据操作的类型以及实际参数值，将SQL操作预处理成相应的数据源与参数映射。
	 * 
	 * @param args
	 *            实际方法调用时的参数
	 * @return 返回数据源与实际SQL以及对应的参数
	 */
	Map<String, List<SqlExecuteBean>> prepareSqlAndParams(Object[] args,
			String defaultDataSource) {
		Map<String, List<SqlExecuteBean>> sqlMap = new HashMap<String, List<SqlExecuteBean>>();
		List<SqlExecuteBean> sqls = null;

		for (SqlToken token : sqlTokens) {
			switch (token.getType()) {
			case STATIC:
				sqls = buildStatic((StaticToken) token, sqls, args,
						defaultDataSource);
				break;
			case TABLE:
				sqls = buildTable((TableToken) token, sqls, args,
						defaultDataSource);
				break;
				
			case TABLE_JOIN:
				sqls = buildTableJoin((TableJoinToken) token, sqls, args,
						defaultDataSource);
				break;
				
			case IN_CLAUSE:
				sqls = buildInClause((InClauseToken) token, sqls, args);
				break;
			case EXPRESSIOIN:
				sqls = buildExpression((ExpressionToken) token, sqls, args);
				break;
			}
		}

		for (SqlExecuteBean bean : sqls) {
			if (!sqlMap.containsKey(bean.getDbType()))
				sqlMap.put(bean.getDbType(), new ArrayList<SqlExecuteBean>());

			sqlMap.get(bean.getDbType()).add(bean);
		}

		sqlMap = filterSqlBean(sqlMap);

		return sqlMap;
	}
	
	
	/**
	 * 根据实际参数以及静态token组装sql。
	 * 
	 * @param token
	 *            要被处理的静态token
	 * @param sqls
	 *            前面步骤处理出来的SQL集合
	 * @param args
	 *            实际的方法参数
	 * @return 返回处理后的SQL集合
	 */
	protected List<SqlExecuteBean> buildStatic(StaticToken token,
			List<SqlExecuteBean> sqls, Object[] args, String defaultDataSource) {
		if (null == sqls) {
			// 如果SQL集合为NULL，表示还没有开始SQL的组装，所以创建一个
			sqls = new ArrayList<SqlExecuteBean>();
			SqlExecuteBean sqlExecuteBean = new SqlExecuteBean();

			if (this.tables.size() == 0) {
				sqlExecuteBean.setDbType(defaultDataSource);
			}

			sqlExecuteBean.appendSql(token.getContent());
			sqlExecuteBean.setExecutor(getDefaultExecutor());
			if (null != args)
				sqlExecuteBean.setArgs(Arrays.asList(args));

			sqls.add(sqlExecuteBean);
		} else {
			// 如果已经存在了，则在所有的sql后面添加静态段
			for (SqlExecuteBean bean : sqls) {
				bean.appendSql(token.getContent());
			}
		}

		return sqls;
	}
	
	
	/**
	 * 根据表的散列方式，构建SQL的表名部分，以及将参数按散列规则重新分组。
	 * 
	 * @param table
	 *            表名token
	 * @param sqls
	 *            已经处理的SQL集合
	 * @param args
	 *            实际方法参数
	 * @param defaultDataSource
	 *            默认的数据源
	 * @return 返回处理的SQL集合
	 */
	protected List<SqlExecuteBean> buildTable(TableToken table,
			List<SqlExecuteBean> sqls, Object[] args, String defaultDataSource) {

		// 如果没有配置表的散列规则，则认为表没有进行过散列
		TablePartition config = DatasourceFactory.getTablePartition(table.getContent());
		
		if(config != null){
			ArgParam keyParam = argParams.get(config.getKeyColumn());

			if (null == keyParam) {
				throw new DaoException("表（" + table + "）的散表关键列（"
						+ config.getKeyColumn() + "）没有被作为方法参数传入。["
						+ getMethodFullName() + "]");
			}
			Object param = keyParam.getValue(args[keyParam.getArgIndex()]);
			sqls = addTable(sqls, DatasourceFactory.getPartitionTableName(table.getContent(), param), defaultDataSource);
		}else{
			sqls = addTable(sqls, table.getContent(), defaultDataSource);
		}
		return sqls;
	}
	

	
	
	
	/**
	 * 简单的向所有已经存在的SQL中添加一个指定的表名，不对参数做散列。
	 * 
	 * @param partitionSqls
	 *            正在处理的SQL集合
	 * @param tableName
	 *            指定的表名
	 * @param dataSource
	 *            指定的表名相关联的数据源
	 * @return 返回处理后的SQL集合
	 */
	protected List<SqlExecuteBean> addTable(List<SqlExecuteBean> partitionSqls,
			String tableName, String dataSource) {
		List<SqlExecuteBean> newSqls = new ArrayList<SqlExecuteBean>();
		for (SqlExecuteBean bean : partitionSqls) {
			// 只有当SQL还没有关联数据源，或者与当前表的数据源相同时才保留此SQL，否则丢弃
			if (bean.getDbType() == null || bean.getDbType().equals(dataSource)) {
				bean.setDbType(dataSource);
				bean.appendSql(tableName);

				newSqls.add(bean);
			}
		}

		return newSqls;
	}
	
	
	protected List<SqlExecuteBean> buildTableJoin(TableJoinToken tableJoin,
			List<SqlExecuteBean> sqls, Object[] args, String defaultDataSource) {

		// 如果没有配置表的散列规则，则认为表没有进行过散列
		TablePartition config = DatasourceFactory.getTablePartition(tableJoin.getTable());
		
		if(config != null){
			ArgParam keyParam = argParams.get(config.getKeyColumn());

			if (null == keyParam) {
				throw new DaoException("表（" + tableJoin.getTable() + "）的散表关键列（"
						+ config.getKeyColumn() + "）没有被作为方法参数传入。["
						+ getMethodFullName() + "]");
			}
			Object param = keyParam.getValue(args[keyParam.getArgIndex()]);
			sqls = addTableJoin(sqls, DatasourceFactory.getPartitionTableName(tableJoin.getTable(), param), tableJoin.getColumn(), defaultDataSource);
		}else{
			sqls = addTableJoin(sqls, tableJoin.getTable(), tableJoin.getColumn(), defaultDataSource);
		}
		return sqls;
	}
	

	protected List<SqlExecuteBean> addTableJoin(List<SqlExecuteBean> partitionSqls,
			String tableName, String column, String dataSource) {
		List<SqlExecuteBean> newSqls = new ArrayList<SqlExecuteBean>();
		for (SqlExecuteBean bean : partitionSqls) {
			// 只有当SQL还没有关联数据源，或者与当前表的数据源相同时才保留此SQL，否则丢弃
			if (bean.getDbType() == null || bean.getDbType().equals(dataSource)) {
				bean.setDbType(dataSource);
				bean.appendSql(tableName + "." + column);

				newSqls.add(bean);
			}
		}

		return newSqls;
	}	
	
	/**
	 * 构建SQL语句中的in操作部分，并指定相应的参数值。
	 * 
	 * @param inClause
	 *            in操作的token
	 * @param sqls
	 *            已经处理的SQL集合
	 * @param args
	 *            实际方法参数
	 * @return 返回处理后的SQL集合
	 */
	protected List<SqlExecuteBean> buildInClause(InClauseToken inClause,
			List<SqlExecuteBean> sqls, Object[] args) {
		List<SqlExecuteBean> newSqls = new LinkedList<SqlExecuteBean>();
		ArgParam arg = inClause.getArg();
		for (SqlExecuteBean bean : sqls) {
			List<Object> inParams = arg.getListValue(bean.getArgs().get(
					arg.getArgIndex()));

			if (inParams.isEmpty())
				continue;

			// 确定sql参数替代符号, 除了字符串以外一律使用问号即可，字符串加单引号
			String paramChar = " ?,";

			// 构建替代字符串，用于组装sql
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < inParams.size(); i++) {
				s.append(paramChar);
			}
			s.deleteCharAt(s.length() - 1);

			bean.appendSql(s.toString());
			bean.getParams().addAll(inParams);

			newSqls.add(bean);
		}

		return newSqls;
	}

	
	/**
	 * 构建SQL语句中的参数部分，并添加实际的参数值
	 * 
	 * @param expression
	 *            参数token
	 * @param sqls
	 *            已经处理的SQL集合
	 * @param args
	 *            实际方法参数
	 * @return 返回处理后的SQL集合
	 */
	protected List<SqlExecuteBean> buildExpression(ExpressionToken expression,
			List<SqlExecuteBean> sqls, Object[] args) {

		for (SqlExecuteBean bean : sqls) {
			bean.appendSql("?");
			ArgParam param = expression.getArg();

			// 如果参数不是bean的集合时添加当前参数；否则不添加，bean集合的参数指定有后续方法处理 (filterSqlBean()方法)
			if (param.getParamType() != DaoParamType.LIST_PARAM_BEAN) {
				bean.getParams()
						.add(param.getValue(bean.getArgs().get(
								param.getArgIndex())));
			}
		}

		return sqls;
	}

	
	
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("Sql :");
		StringBuilder t = new StringBuilder();
		for (SqlToken token : this.sqlTokens) {
			switch (token.getType()) {
			case STATIC:
				s.append(" ").append(token.getContent());
				t.append("\nStatic part: " + token.getContent());
				break;
			case TABLE:
				s.append(" ").append(token.getContent());
				t.append("\nTable: " + token.getContent());
				break;
			case IN_CLAUSE:
				s.append(" ").append(token.getContent());
				t.append("\nIn clause: " + token.getContent());
				break;
			case EXPRESSIOIN:
				s.append(" ").append(token.getContent());
				t.append("\nExpression: " + token.getContent());
				break;
			}
		}

		return t.append("\n").append(s).toString();
	}

}
