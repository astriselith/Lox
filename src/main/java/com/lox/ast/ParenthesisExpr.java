package com.lox.ast;

import com.lox.util.Position;

public class ParenthesisExpr extends Expr {
    public final Expr expression;
    
    public ParenthesisExpr(Expr expression, Position position) {
        super(position);
        this.expression = expression;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitParenthesisExpr(this);
    }
}