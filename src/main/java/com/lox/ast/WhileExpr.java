package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class WhileExpr extends Expr {
    public final Expr condition;
    public final List<Expr> body;
    
    public WhileExpr(Expr condition, List<Expr> body, Position position) {
        super(position);
        this.condition = condition;
        this.body = body;
    }
}