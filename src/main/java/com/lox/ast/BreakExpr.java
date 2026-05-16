package com.lox.ast;

import com.lox.util.Position;

public class BreakExpr extends Expr {
    public BreakExpr(Position position) {
        super(position);
    }
    
    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitBreakExpr(this);
    }
}