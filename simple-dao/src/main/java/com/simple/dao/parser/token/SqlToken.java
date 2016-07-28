package com.simple.dao.parser.token;

/**
 * SQL语句中的各种类型的语句块。
 * @since 1.0
 */
public interface SqlToken {

    public enum TokenType {STATIC, TABLE, IN_CLAUSE, EXPRESSIOIN, TABLE_JOIN}

    /**
     * 获得当前token的类型。
     * @see TokenType
     * @return 返回当前token的类型
     */
    public TokenType getType();

    /**
     * 获得当前token的原始内容
     * @return 返回当前token包含的原始sql语句部分
     */
    public String getContent();

}
