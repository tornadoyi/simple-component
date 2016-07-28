package com.simple.base.util.expression;



/**
 * 二元操作表达式。
 * @since 1.0
 */
public abstract class BinaryOperationExpression implements Expression {

    private static final long serialVersionUID = -176241089133898830L;
    
    protected Expression left;
    protected Expression right;

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public abstract int getPriority();

}
