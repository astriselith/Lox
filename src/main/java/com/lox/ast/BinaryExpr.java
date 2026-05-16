package com.lox.ast;

import com.lox.util.Position;

public class BinaryExpr extends Expr {
 public final Expr left;
 public final String operator;
 public final Expr right;

 public BinaryExpr(Expr left, String operator, Expr right, Position position) {
		super(position);
		this.left = left;
		this.operator = operator;
		this.right = right;
 }
}