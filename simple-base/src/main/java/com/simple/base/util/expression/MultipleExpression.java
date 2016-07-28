package com.simple.base.util.expression;


/**
 * 乘法操作。
 * @since 1.0
 */
public class MultipleExpression extends BinaryOperationExpression {

    private static final long serialVersionUID = -5663414391707378304L;

    @Override
    public long getValue(long key) {
        return left.getValue(key) * right.getValue(key);
    }

    @Override
    public int getPriority() {
        return 2;
    }

}
