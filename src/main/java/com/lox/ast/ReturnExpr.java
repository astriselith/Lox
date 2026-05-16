package com.lox.ast;

import com.lox.util.Position;

public class ReturnExpr extends Expr {
    public final Expr value;
    public final FunDeclExpr enclosingFunction;
    
    public ReturnExpr(Expr value, Position position) {
        this(value, null, position);
    }
    
    public ReturnExpr(Expr value, FunDeclExpr enclosingFunction, Position position) {
        super(position);
        this.value = value;
        this.enclosingFunction = enclosingFunction;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitReturnExpr(this);
    }
}