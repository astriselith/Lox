package com.lox.ast;

import com.lox.util.Position;

public class SetExpr extends Expr {
    public final Expr object;
    public final String name;
    public final Expr value;
    
    public SetExpr(Expr object, String name, Expr value, Position position) {
        super(position);
        this.object = object;
        this.name = name;
        this.value = value;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitSetExpr(this);
    }
}