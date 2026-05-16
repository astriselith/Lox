package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class CallExpr extends Expr {
    public final Expr callee;
    public final List<Expr> arguments;
    
    public CallExpr(Expr callee, List<Expr> arguments, Position position) {
        super(position);
        this.callee = callee;
        this.arguments = arguments;
    }
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }
}