package com.lox.ast;

import com.lox.util.Position;

public class TernaryExpr extends Expr {
    public final Expr condition;
    public final Expr thenExpr;
    public final Expr elseExpr;
    
    public TernaryExpr(Expr condition, Expr thenExpr, Expr elseExpr, Position position) {
        super(position);
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitTernaryExpr(this);
    }
}