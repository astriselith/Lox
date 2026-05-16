package com.lox.ast;

import com.lox.util.Position;

public class LiteralExpr extends Expr {
    public final Object value;
    
    public LiteralExpr(Object value, Position position) {
        super(position);
        this.value = value;
    }
}