package com.simple.base.util.expression;


/**
 * 除法操作。
 * @since 1.0
 */
public class DivideExpression extends BinaryOperationExpression {

    private static final long serialVersionUID = -6210937060757444787L;

    @Override
    public long getValue(long key) {
        return left.getValue(key) / right.getValue(key);
    }

    @Override
    public int getPriority() {
        return 2;
    }

}
