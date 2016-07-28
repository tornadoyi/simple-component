package com.simple.base.util.expression;



/**
 * 静态数值表达式。
 * @since 1.0
 */
public class StaticExpression implements Expression {

    private static final long serialVersionUID = -6060431150586251801L;

    private int value;

    public StaticExpression(int value) {
        this.value = value;
    }

    @Override
    public long getValue(long key) {
        return value;
    }

}
