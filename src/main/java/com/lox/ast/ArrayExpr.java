package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class ArrayExpr extends Expr {
    public final List<Expr> elements;
    
    public ArrayExpr(List<Expr> elements, Position position) {
        super(position);
        this.elements = elements;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitArrayExpr(this);
    }
}