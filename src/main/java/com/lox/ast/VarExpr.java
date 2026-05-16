package com.lox.ast;

import com.lox.util.Position;

public class VarExpr extends Expr {
    public final String name;
    
    public VarExpr(String name, Position position) {
        super(position);
        this.name = name;
    }
}