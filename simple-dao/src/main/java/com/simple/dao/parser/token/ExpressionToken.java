package com.simple.dao.parser.token;

import com.simple.dao.bean.ArgParam;

/**
 * SQL语句中待定参数的部分。
 *
 
 * @version 1.0 2009-10-30 10:51:47
 * @since 1.0
 */
public class ExpressionToken extends AbstractSqlToken {

    private String initialName;

    private String paramName;

    private ArgParam arg = null;

    public ExpressionToken(String paramName) {

        if (!paramName.startsWith(":"))
            throw new RuntimeException(paramName + " is not a valid expression in SQL.");

        this.initialName = paramName;
        this.paramName = initialName.substring(1);
    }

    public ArgParam getArg() {
        return arg;
    }

    public void setArg(ArgParam arg) {
        this.arg = arg;
    }

    public String getParamName() {
        return paramName;
    }

    @Override
    public String getContent() {
        return initialName;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof ExpressionToken)) {
            return false;
        }

        ExpressionToken token = (ExpressionToken) obj;

        return initialName.equals(token.initialName);
    }

    @Override
    public TokenType getType() {
        return TokenType.EXPRESSIOIN;
    }

    @Override
    public String toString() {
        return "Expression token, name is " + initialName;
    }

}
