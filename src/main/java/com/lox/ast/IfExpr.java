package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class IfExpr extends Expr {
    public final Expr condition;
    public final List<Expr> thenBranch;
    public final List<Expr> elseBranch;
    
    public IfExpr(Expr condition, List<Expr> thenBranch, List<Expr> elseBranch, Position position) {
        super(position);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}