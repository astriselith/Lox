package com.lox.ast;

import com.lox.util.Position;

public class VarDeclExpr extends Expr {
    public final String name;
    public final Expr value;
    public final boolean isConst;
    
    public VarDeclExpr(String name, Expr value, Position position) {
        this(name, value, false, position);
    }
    
    public VarDeclExpr(String name, Expr value, boolean isConst, Position position) {
        super(position);
        this.name = name;
        this.value = value;
        this.isConst = isConst;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitVarDeclExpr(this);
    }
}