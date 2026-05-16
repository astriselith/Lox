package com.lox.ast;

import com.lox.util.Position;

public class GetExpr extends Expr {
    public final Expr object;
    public final String name;
    
    public GetExpr(Expr object, String name, Position position) {
        super(position);
        this.object = object;
        this.name = name;
    }
}