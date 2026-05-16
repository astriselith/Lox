package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class VarDeclExpr extends Expr {
 public final String name;
 public final Expr value;
 public final List<Expr> decorators;

 public VarDeclExpr(String name, Expr value, List<Expr> decorators, Position position) {
		super(position);
		this.name = name;
		this.value = value;
		this.decorators = decorators != null ? decorators : List.of();
 }
}