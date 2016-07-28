package com.simple.dao.parser.token;



/**
 * SQL语句中表名的部分。
 *
 
 * @version 1.0 2009-10-28 13:50:50
 * @since 1.0
 */
public class TableToken extends AbstractSqlToken {

    private String name;

    public TableToken(String name) {
        this.name = name;
    }

    @Override
    public String getContent() {
        return name;
    }

    @Override
    public TokenType getType() {
        return TokenType.TABLE;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof TableToken)) {
            return false;
        }

        return ((TableToken) obj).name.equals(name);
    }

    @Override
    public String toString() {
        return "Table token " + getContent();
    }

}
