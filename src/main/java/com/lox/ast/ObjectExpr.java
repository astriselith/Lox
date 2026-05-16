package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class ObjectExpr extends Expr {
 public final List<Expr> declarations;

 public ObjectExpr(List<Expr> declarations, Position position) {
		super(position);
		this.declarations = declarations != null ? declarations : List.of();
 }
}