package com.simple.base.util.expression;

/**
 * 减法操作。
 * @since 1.0
 */
public class SubtractExpression extends BinaryOperationExpression {

    private static final long serialVersionUID = 1026380096191594448L;

    @Override
    public long getValue(long key) {
        return left.getValue(key) - right.getValue(key);
    }

    @Override
    public int getPriority() {
        return 1;
    }

}
