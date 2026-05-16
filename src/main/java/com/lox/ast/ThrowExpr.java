package com.lox.ast;

import com.lox.util.Position;

public class ThrowExpr extends Expr {
 public final Expr value;
 public final FunDeclExpr enclosingFunction;

 public ThrowExpr(Expr value, Position position) {
		this(value, null, position);
 }

 public ThrowExpr(Expr value, FunDeclExpr enclosingFunction, Position position) {
		super(position);
		this.value = value;
		this.enclosingFunction = enclosingFunction;
 }
}