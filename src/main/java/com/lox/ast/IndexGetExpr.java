package com.lox.ast;

import com.lox.util.Position;

public class IndexGetExpr extends Expr {
    public final Expr array;
    public final Expr index;
    
    public IndexGetExpr(Expr array, Expr index, Position position) {
        super(position);
        this.array = array;
        this.index = index;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitIndexGetExpr(this);
    }
}