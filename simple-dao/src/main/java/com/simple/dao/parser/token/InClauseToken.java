package com.simple.dao.parser.token;

import com.simple.dao.bean.ArgParam;



/**
 * SQL语句中in操作的参数占位符部分。
 *
 * @since 1.0
 */
public class InClauseToken extends AbstractSqlToken {

    private String paramName;

    private ArgParam arg;

    public InClauseToken(String paramName) {
        this.paramName = paramName;
    }

    public ArgParam getArg() {
        return arg;
    }

    public void setArg(ArgParam arg) {
        this.arg = arg;
    }

    @Override
    public String getContent() {
        return paramName;
    }

    @Override
    public TokenType getType() {
        return TokenType.IN_CLAUSE;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof InClauseToken)) {
            return false;
        }

        InClauseToken token = (InClauseToken) obj;
        return paramName.equals(token.paramName);
    }

    @Override
    public String toString() {
        return "In clause token, name is " + paramName;
    }


}
