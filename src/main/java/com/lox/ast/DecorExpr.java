package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class DecorExpr extends Expr {
 public final String name;
 public final List<Expr> arguments;

 public DecorExpr(String name, List<Expr> arguments, Position position) {
		super(position);
		this.name = name;
		this.arguments = arguments != null ? arguments : List.of();
 }
}