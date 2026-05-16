package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class BlockExpr extends Expr {
    private final List<Expr> expressions;
    
    public BlockExpr(List<Expr> expressions, Position position) {
        super(position);
        this.expressions = expressions;
    }
    
    public List<Expr> getExpressions() {
        return expressions;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitBlockExpr(this);
    }
}