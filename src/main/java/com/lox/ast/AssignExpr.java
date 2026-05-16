package com.lox.ast;

import com.lox.util.Position;

public class AssignExpr extends Expr {
 public final String name;
 public final Expr value;

 public AssignExpr(String name, Expr value, Position position) {
		super(position);
		this.name = name;
		this.value = value;
 }
}