package com.lox.ast;

import com.lox.util.Position;

public abstract class Expr {
    protected Position position;
    
    protected Expr() {
        this(null);
    }
    
    protected Expr(Position position) {
        this.position = position;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = position;
    }
    
    public abstract <T> T accept(ExprVisitor<T> visitor);
}