package com.simple.dao.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.simple.dao.core.OperationType;
import com.simple.dao.error.DaoException;
import com.simple.dao.parser.token.ExpressionToken;
import com.simple.dao.parser.token.InClauseToken;
import com.simple.dao.parser.token.SqlToken;
import com.simple.dao.parser.token.SqlToken.TokenType;
import com.simple.dao.parser.token.StaticToken;
import com.simple.dao.parser.token.TableJoinToken;
import com.simple.dao.parser.token.TableToken;


/**
 * SQL解析器的简单实现，只解析表名、参数占位符以及in操作占位符，其他部分一律当做不变部分看待，不对SQL语法验证。
 * @since 1.0
 */
public class SimpleSqlParser {

    /**
     * 判断SQL类型。
     * @param sql 待判定的完整SQL
     * @return 返回SQL语句的类型，如果不是合法的类型则返回NULL
     */
    public static OperationType getOperationType(String sql) {
        sql = sql.trim();
        int index = sql.indexOf(" ");

        return OperationType.valueOf(sql.substring(0, index).toUpperCase());
    }

    /**
     * 解析特定类型的SQL语句。
     * @param sql SQL语句
     * @param type SQL类型
     * @return 返回解析后的SqlToken集合
     */
    public static List<SqlToken> parserSql(String sql, OperationType type) {
        String[] sqlParts = sql.trim().split("\\s+");
        LinkedList<String> sqlPartQueue = new LinkedList<String>(Arrays.asList(sqlParts));
        List<SqlToken> tokens = null;

        switch (type) {
            case SELECT:
                tokens = SelectSqlParser.getInstance().parserToken(sqlPartQueue);
                break;
            case UPDATE:
                tokens = UpdateSqlParser.getInstance().parserToken(sqlPartQueue);
                break;
            case DELETE:
                tokens = DeleteSqlParser.getInstance().parserToken(sqlPartQueue);
                break;
            case INSERT:
            case REPLACE:
                tokens = InsertSqlParser.getInstance().parserToken(sqlPartQueue);
                break;
           
            default:
                throw new DaoException("Invalid sql type " + sqlParts[0] + ", sql is " + sql);
        }

        return arrangeToken(tokens);
    }

    /**
     * 将相邻的静态token合并。
     * @param initialTokens 原始的token集合
     * @return 返回合并后的token集合
     */
    private static List<SqlToken> arrangeToken(List<SqlToken> initialTokens) {
        if (null == initialTokens) return null;

        boolean mergeStatic = false;
        List<SqlToken> tokens = new ArrayList<SqlToken>();
        StaticToken currentStaticToken = new StaticToken("");

        for (SqlToken token : initialTokens) {
            if (token.getType() == TokenType.STATIC) {
                if (mergeStatic) {
                    currentStaticToken.addContent(" " + token.getContent());
                } else {
                    currentStaticToken = (StaticToken) token;
                    tokens.add(token);
                    mergeStatic = true;
                }
            } else {
                tokens.add(token);

                if (mergeStatic) {
                    mergeStatic = false;
                    currentStaticToken = null;
                }
            }
        }

        return tokens;
    }

    /**
     * SQL解析接口。
 
     * @version 1.0 2009-11-04 10:47:41
     * @since 1.0
     */
    private static interface SqlParser {

        /**
         * 解析sql序列。
         * @param sqlParts 以空字符切分的sql语句块
         * @return 返回解析后的SQL序列
         */
        public List<SqlToken> parserToken(LinkedList<String> sqlParts);
    }

    /**
     * SQL解析类的抽象实现。
 
     * @version 1.0 2009-11-04 10:49:59
     * @since 1.0
     */
    private static abstract class AbstractSqlParser implements SqlParser {

        /**
         * 如果SQL语句剩下部分只包含参数部分时，可以直接使用此方法解析参数部分的内容。
         * @param parts SQL语句为解析的序列
         * @return 返回SQL解析后的序列
         */
        protected List<SqlToken> parseParamToTheEnd(LinkedList<String> parts) {
            List<SqlToken> whereClause = new ArrayList<SqlToken>();

            while (parts.peek() != null) {
                String s = parts.peek();

                if (s.contains(":")) {
                    whereClause.addAll(parseParam(parts));
                    continue;
                }

                if (s.contains("$")) {
                    whereClause.addAll(parseInParam(parts));
                    continue;
                }

                whereClause.add(new StaticToken(parts.poll()));
            }

            return whereClause;
        }

        /**
         * 解析普通的参数占位符。
         * @param parts 为解析的SQL序列
         * @return 返回解析后的SQL序列
         */
        protected List<SqlToken> parseParam(LinkedList<String> parts) {
            List<SqlToken> tokens = new ArrayList<SqlToken>();
            String s = parts.poll();
            int index = s.indexOf(":");

            //如果在“：”前有字符，则作为静态处理
            if (index > 0) {
                tokens.add(new StaticToken(s.substring(0, index)));
            }

            String sub = s.substring(index);

            int commaIndex = -1;
            //如果参数占位符后面有“，”或者“）”则将占位符后面的部分放回SQL序列首部
            if ((commaIndex = sub.indexOf(",")) != -1
                    || (commaIndex = sub.indexOf(")")) != -1) {
                tokens.add(new ExpressionToken(sub.substring(0, commaIndex)));
                parts.addFirst(sub.substring(commaIndex));
            } else {
                tokens.add(new ExpressionToken(sub));
            }

            return tokens;
        }

        /**
         * 解析in操作的参数占位符。
         * @param parts 为解析的SQL序列
         * @return 返回解析后的SQL序列
         */
        protected List<SqlToken> parseInParam(LinkedList<String> parts) {
            List<SqlToken> tokens = new ArrayList<SqlToken>();
            String s = parts.poll();
            int index = s.indexOf("$");

            //如果在“$”前有字符，则作为静态处理
            if (index > 0) {
                tokens.add(new StaticToken(s.substring(0, index)));
            }

            String sub = s.substring(index);
            //如果参数占位符后面有“）”则将占位符后面的部分放回SQL序列首部
            if (sub.contains(")")) {
                int i = sub.indexOf(")");
                tokens.add(new InClauseToken(sub.substring(0, i)));
                parts.addFirst(sub.substring(i));
            } else {
                tokens.add(new InClauseToken(sub));
            }

            return tokens;
        }
    }


    /**
     * 解析查询操作的SQL语句，例如：select * from rc_user_info where user_id = ?
     *
 
     * @version 1.0 2009-10-29 13:52:37
     * @since 1.0
     */
    private static class SelectSqlParser extends AbstractSqlParser {

        private static final SelectSqlParser instance = new SelectSqlParser();

        private SelectSqlParser() {}

        public static SelectSqlParser getInstance() {
            return instance;
        }

        @Override
        public List<SqlToken> parserToken(LinkedList<String> sql) {
            if (sql.peek().equalsIgnoreCase("select")) {
                List<SqlToken> tokens = new ArrayList<SqlToken>();
                //解析查询部分，全部作为静态处理
                tokens.add(parseSelectClause(sql));
                //解析查询表的部分
                tokens.addAll(parseTableClause(sql));
                tokens.addAll(parseTableJoin(sql));
                tokens.addAll(parseParamToTheEnd(sql));

                return tokens;
            }

            return null;
        }

        //select test1.* from test1 left join test2 on test1.id = test2.id where test1.id > 0
        private List<SqlToken> parseTableJoin(LinkedList<String> parts){
        	List<SqlToken> tableJoinTokens = new ArrayList<SqlToken>();
        	if(parts.peek().equalsIgnoreCase("on")){
        		String partName = null;
        		 while (parts.peek() != null
                         && !parts.peek().equalsIgnoreCase("where")
                         && !parts.peek().equalsIgnoreCase("order")
                         && !parts.peek().equalsIgnoreCase("limit")
                         && !parts.peek().equalsIgnoreCase("group")) {
        			 partName = parts.poll();
        			 int index = -1;
        			 if(partName.equalsIgnoreCase("on")){
        				 tableJoinTokens.add(new StaticToken(partName));
        			 }else if(partName.startsWith("=")){
        				 index = partName.indexOf("=");
        				 tableJoinTokens.add(new StaticToken("="));
        				 if (index < partName.length() - 1) {
                              parts.addFirst(partName.substring(index + 1));
                         }
        			 }else if((index = partName.indexOf("=")) != -1){
        				 String tableName = partName.substring(0, index).split("\\.")[0];
        				 String column = partName.substring(0, index).split("\\.")[1];
        				 tableJoinTokens.add(new TableJoinToken(tableName, column));
        				 tableJoinTokens.add(new StaticToken("="));
                         if (index < partName.length() - 1) {
                             parts.addFirst(partName.substring(index + 1));
                         }
        			 }else{
        				 String tableName = partName.split("\\.")[0];
        				 String column = partName.split("\\.")[1];
        				 tableJoinTokens.add(new TableJoinToken(tableName, column));
        			 }
        		 }
            
        	}
        	
        	return tableJoinTokens;
        }
        
        
        /**
         * 查询SQL中必须以“from”关键字来区分结果部分和表部分。
         * @param parts sql语句块队列
         * @return 返回查询结果段
         */
        private StaticToken parseSelectClause(LinkedList<String> parts) {
            //解析查询结果
            StaticToken staticToken = new StaticToken(parts.poll());
            for (String content = parts.peek(); content != null && !content.equalsIgnoreCase("from");
                    content = parts.peek()) {
                staticToken.addContent(" " + parts.poll());
            }
            staticToken.addContent(" " + parts.poll());

            return staticToken;
        }

        
     
        
        
        /**
         * 解析表部分的SQL。
         * @param parts 未解析的SQL序列
         * @return 返回解析后的SQL序列
         */
        private List<SqlToken> parseTableClause(LinkedList<String> parts) {
            List<SqlToken> tableTokens = new ArrayList<SqlToken>();
            String partName = null;
            boolean tablePart = true;

            while (parts.peek() != null
                    && !parts.peek().equalsIgnoreCase("where")
                    && !parts.peek().equalsIgnoreCase("on")
                    && !parts.peek().equalsIgnoreCase("order")
                    && !parts.peek().equalsIgnoreCase("limit")
                    && !parts.peek().equalsIgnoreCase("group")) {
                partName = parts.poll();
                int index = -1;
                if (tablePart) {
                    //表名部分，只可能使用“，”与后面的语句部分连在一起，所有只需要将“，”后的部分放回队列首
                	//例: select * from  a,b where xxx=yyyy
                    if ((index = partName.indexOf(",")) != -1) {
                        tableTokens.add(new TableToken(partName.substring(0, index)));
                        tableTokens.add(new StaticToken(","));

                        if (index < partName.length() - 1) {
                            parts.addFirst(partName.substring(index + 1));
                        }

                        tablePart = true;
                    } else {
                        tableTokens.add(new TableToken(partName));
                        tablePart = false;
                    }
                } else {
                    //表连接部分，允许有“，”、“left join”、“join”、“right join”四种
                    if (partName.equalsIgnoreCase("join")) {
                        tableTokens.add(new StaticToken(partName));
                    } else if (partName.equalsIgnoreCase("left") || partName.equalsIgnoreCase("right")) {
                        String secondPart = parts.poll();
                        if (secondPart != null && secondPart.equalsIgnoreCase("join")) {
                            tableTokens.add(new StaticToken(partName + " " + secondPart));
                        } else {
                            throw new DaoException("Invalid table link type near " + partName);
                        }
                    } else if (partName.startsWith(",")) {//例：select * from a ,b where 
                        tableTokens.add(new StaticToken(","));

                        int commaIndex = partName.indexOf(",");
                        if (commaIndex < partName.length() - 1) {
                            parts.addFirst(partName.substring(commaIndex + 1));
                        }

                    } else {
                        throw new DaoException("Invalid table clause near " + partName);
                    }

                    tablePart = true;
                }
            }

            return tableTokens;
        }

    }

    /**
     * 解析插入操作的SQL语句，例如：insert into rc_user_info(id, name, cash) values (?, ?, ?)
     *
 
     * @version 1.0 2009-10-29 13:57:51
     * @since 1.0
     */
    private static class InsertSqlParser extends AbstractSqlParser {

        private static final InsertSqlParser instance = new InsertSqlParser();

        private InsertSqlParser() {}

        public static InsertSqlParser getInstance() {
            return instance;
        }

        @Override
        public List<SqlToken> parserToken(LinkedList<String> sql) {
            if (sql.peek().equalsIgnoreCase("insert")
                    || sql.peek().equalsIgnoreCase("replace")) {
                List<SqlToken> sqlTokens = new ArrayList<SqlToken>();
                //插入语句的头部（insert into）
                sqlTokens.add(parseInsertHead(sql));
                //插入语句的表部分，只允许一个
                sqlTokens.addAll(parseTableClause(sql));
                //剩下部分直接解析参数
                sqlTokens.addAll(parseParamToTheEnd(sql));

                return sqlTokens;
            }

            return null;
        }

        /**
         * 解析插入操作的头部（insert into）
         * @param sql 未解析的SQL序列
         * @return 解析后的SQL序列
         */
        private StaticToken parseInsertHead(LinkedList<String> sql) {
            StringBuilder s = new StringBuilder(sql.poll());

            while (sql.peek() != null && !sql.peek().equalsIgnoreCase("into")) {
                s.append(" ").append(sql.poll());
            }

            s.append(" ").append(sql.poll());

            return new StaticToken(s.toString());
        }

        /**
         * 解析插入操作的表名部分。
         * @param sql 未解析的SQL序列
         * @return 解析后的SQL序列
         */
        private List<SqlToken> parseTableClause(LinkedList<String> sql) {
            List<SqlToken> tokens = new ArrayList<SqlToken>();

            String tableName = sql.poll();

            //因为插入操作之允许一个表，所以当前部分肯定包含表名，
            int index = tableName.indexOf("(");

            if (index == -1) {
                tokens.add(new TableToken(tableName));
            } else {
                tokens.add(new TableToken(tableName.substring(0, index)));
                sql.addFirst(tableName.substring(index));
            }

            return tokens;
        }
    }

    /**
     * 更新操作的SQL解析， 例如：update rc_user_info set name = ? where id = ?
     *
 
     * @version 1.0 2009-10-29 13:59:35
     * @since 1.0
     */
    private static class UpdateSqlParser extends AbstractSqlParser {

        private static final UpdateSqlParser instance = new UpdateSqlParser();

        private UpdateSqlParser() {}

        public static UpdateSqlParser getInstance() {
            return instance;
        }

        @Override
        public List<SqlToken> parserToken(LinkedList<String> sql) {
            if (sql.peek().equalsIgnoreCase("update")) {

                List<SqlToken> tokens = new ArrayList<SqlToken>();
                tokens.add(new StaticToken(sql.poll()));
                // parse table
                tokens.add(new TableToken(sql.poll()));
                // parse set clause
                tokens.addAll(parseSetClause(sql));
                // parse where clause
                tokens.addAll(parseParamToTheEnd(sql));

                return tokens;
            }

            return null;
        }

        /**
         * 解析更新操作中的参数设置部分。
         * @param sql 未解析的SQL序列
         * @return 返回解析后的SQL序列
         */
        private List<SqlToken> parseSetClause(LinkedList<String> sql) {
            List<SqlToken> tokens = new ArrayList<SqlToken>();

            tokens.add(new StaticToken(sql.poll()));

            while (sql.peek() != null && !sql.peek().equalsIgnoreCase("where")) {
                String s = sql.peek();

                if (s.contains(":")) {
                    tokens.addAll(parseParam(sql));
                    continue;
                }

                if (s.contains("$")) {
                    tokens.addAll(parseInParam(sql));
                    continue;
                }

                tokens.add(new StaticToken(sql.poll()));
            }

            return tokens;
        }

    }

    /**
     * 删除操作的SQL解析，例如： delete from rc_user_info where id = ?
     *
 
     * @version 1.0 2009-10-29 14:01:21
     * @since 1.0
     */
    private static class DeleteSqlParser extends AbstractSqlParser {

        private static final DeleteSqlParser instance = new DeleteSqlParser();

        private DeleteSqlParser() {}

        public static DeleteSqlParser getInstance() {
            return instance;
        }

        @Override
        public List<SqlToken> parserToken(LinkedList<String> sql) {

            if (sql.peek().equalsIgnoreCase("delete")
                    || sql.peek().equalsIgnoreCase("lock")) {
                //删除操作开头必须是 delete from
                List<SqlToken> tokens = new ArrayList<SqlToken>();
                StringBuilder s = new StringBuilder(sql.poll());
                s.append(" ").append(sql.poll());
                tokens.add(new StaticToken(s.toString()));
                tokens.add(new TableToken(sql.poll()));
                tokens.addAll(parseParamToTheEnd(sql));

                return tokens;
            }

            return null;
        }

    }

    /**
     * 锁表操作。
     * 由于锁表操作与删除操作类似，所以直接继承自删除操作。
 
     * @version 1.0 2010-04-27 15:59:40
     * @since 1.0
     */
    private static class LockSqlParser extends DeleteSqlParser {

        private static final LockSqlParser instance = new LockSqlParser();

        private LockSqlParser() {}

        public static LockSqlParser getInstance() {
            return instance;
        }
    }

    /**
     * 静态SQL，整个SQL语句是固定。
 
     * @version 1.0 2010-04-27 15:51:14
     * @since 1.0
     */
    private static class StaticSqlParser extends AbstractSqlParser {

        private static final StaticSqlParser instance = new StaticSqlParser();

        private StaticSqlParser() {}

        public static StaticSqlParser getInstance() {
            return instance;
        }

        @Override
        public List<SqlToken> parserToken(LinkedList<String> sqlParts) {
            List<SqlToken> tokens = new ArrayList<SqlToken>();
            StringBuilder s = new StringBuilder();

            for (String sqlPart : sqlParts) {
                s.append(sqlPart).append(" ");
            }

            tokens.add(new StaticToken(s.toString()));
            return tokens;
        }

    }

    /**
     * 解析truncate类型的SQL
 
     * @version 1.0 2010-04-27 16:04:22
     * @since 1.0
     */
    private static class TruncateSqlParser extends AbstractSqlParser {

        private static final TruncateSqlParser instance = new TruncateSqlParser();

        private TruncateSqlParser() {}

        public static TruncateSqlParser getInstance() {
            return instance;
        }

        @Override
        public List<SqlToken> parserToken(LinkedList<String> sqlParts) {
            List<SqlToken> tokens = new ArrayList<SqlToken>();

            tokens.add(new StaticToken(sqlParts.poll()));
            tokens.add(new TableToken(sqlParts.poll()));

            return tokens;
        }
    }

}
