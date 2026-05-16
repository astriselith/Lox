package com.lox.ast;

import com.lox.util.Position;

public class UnaryExpr extends Expr {
    public static final int PREFIX = 1;
    public static final int POSTFIX = 2;
    
    public final String operator;
    public final Expr operand;
    public final int type;
    
    public UnaryExpr(String operator, Expr operand, Position position) {
        this(operator, operand, PREFIX, position);
    }
    
    public UnaryExpr(String operator, Expr operand, int type, Position position) {
        super(position);
        this.operator = operator;
        this.operand = operand;
        this.type = type;
    }
    
    public boolean isPrefix() {
        return type == PREFIX;
    }
    
    public boolean isPostfix() {
        return type == POSTFIX;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}