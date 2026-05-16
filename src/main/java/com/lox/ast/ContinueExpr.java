package com.lox.ast;

import com.lox.util.Position;

public class ContinueExpr extends Expr {
    public ContinueExpr(Position position) {
        super(position);
    }
    
    @Override
    public <R> R accept(ExprVisitor<R> visitor) {
        return visitor.visitContinueExpr(this);
    }
}