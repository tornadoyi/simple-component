package com.simple.dao.parser.token;



/**
 * SQL语句中不会被改变的语句部分，即不是待定参数或者表名。
 * @since 1.0
 */
public class StaticToken extends AbstractSqlToken {

    private StringBuilder content = new StringBuilder();

    public StaticToken(String content) {
        this.content.append(content);
    }

    public void addContent(String appendContent) {
        this.content.append(appendContent);
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof StaticToken)) {
            return false;
        }

        return ((StaticToken) obj).content.toString().equals(content.toString());
    }

    @Override
    public String getContent() {
        return content.toString();
    }

    @Override
    public TokenType getType() {
        return TokenType.STATIC;
    }

    @Override
    public String toString() {
        return "Static token " + getContent();
    }

}
