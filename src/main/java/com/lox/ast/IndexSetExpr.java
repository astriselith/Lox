package com.lox.ast;

import com.lox.util.Position;

public class IndexSetExpr extends Expr {
    public final Expr array;
    public final Expr index;
    public final Expr value;
    
    public IndexSetExpr(Expr array, Expr index, Expr value, Position position) {
        super(position);
        this.array = array;
        this.index = index;
        this.value = value;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitIndexSetExpr(this);
    }
}