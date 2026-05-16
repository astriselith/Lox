package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class ObjectExpr extends Expr {
    private final List<Expr> expressions;
    
    public ObjectExpr(List<Expr> expressions, Position position) {
        super(position);
        this.expressions = expressions;
    }
    
    public List<Expr> getExpressions() {
        return expressions;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitObjectExpr(this);
    }
}