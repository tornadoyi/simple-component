package com.simple.base.util.expression;



/**
 * 参数占位符表达式，直接返回参数。
 * @since 1.0
 */
public class ParamExpression implements Expression {

    private static final long serialVersionUID = -1691851142296564067L;

    @Override
    public long getValue(long key) {
        return key;
    }

}
