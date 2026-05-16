package com.lox.ast;

import com.lox.util.Position;

public abstract class Expr {
 protected Position position;

 protected Expr() {
		this(null);
 }

 protected Expr(Position position) {
		this.position = position;
 }

 public Position getPosition() {
		return position;
 }

 public void setPosition(Position position) {
		this.position = position;
 }

 public <T> T accept(ExprVisitor<T> visitor) {
		if (this instanceof LiteralExpr) {
			return visitor.visitLiteralExpr((LiteralExpr) this);
		}
		if (this instanceof DecorExpr) {
			return visitor.visitDecorExpr((DecorExpr) this);
		}
		if (this instanceof VarExpr) {
			return visitor.visitVarExpr((VarExpr) this);
		}
		if (this instanceof AssignExpr) {
			return visitor.visitAssignExpr((AssignExpr) this);
		}
		if (this instanceof BinaryExpr) {
			return visitor.visitBinaryExpr((BinaryExpr) this);
		}
		if (this instanceof UnaryExpr) {
			return visitor.visitUnaryExpr((UnaryExpr) this);
		}
		if (this instanceof LogicalExpr) {
			return visitor.visitLogicalExpr((LogicalExpr) this);
		}
		if (this instanceof TernaryExpr) {
			return visitor.visitTernaryExpr((TernaryExpr) this);
		}
		if (this instanceof CallExpr) {
			return visitor.visitCallExpr((CallExpr) this);
		}
		if (this instanceof GetExpr) {
			return visitor.visitGetExpr((GetExpr) this);
		}
		if (this instanceof SetExpr) {
			return visitor.visitSetExpr((SetExpr) this);
		}
		if (this instanceof IndexGetExpr) {
			return visitor.visitIndexGetExpr((IndexGetExpr) this);
		}
		if (this instanceof IndexSetExpr) {
			return visitor.visitIndexSetExpr((IndexSetExpr) this);
		}
		if (this instanceof ArrayExpr) {
			return visitor.visitArrayExpr((ArrayExpr) this);
		}
		if (this instanceof ObjectExpr) {
			return visitor.visitObjectExpr((ObjectExpr) this);
		}
		if (this instanceof VarDeclExpr) {
			return visitor.visitVarDeclExpr((VarDeclExpr) this);
		}
		if (this instanceof FunDeclExpr) {
			return visitor.visitFunDeclExpr((FunDeclExpr) this);
		}
		if (this instanceof ReturnExpr) {
			return visitor.visitReturnExpr((ReturnExpr) this);
		}
		if (this instanceof ThrowExpr) {
			return visitor.visitThrowExpr((ThrowExpr) this);
		}
		if (this instanceof IfExpr) {
			return visitor.visitIfExpr((IfExpr) this);
		}
		if (this instanceof WhileExpr) {
			return visitor.visitWhileExpr((WhileExpr) this);
		}
		if (this instanceof BreakExpr) {
			return visitor.visitBreakExpr((BreakExpr) this);
		}
		if (this instanceof ContinueExpr) {
			return visitor.visitContinueExpr((ContinueExpr) this);
		}
		if (this instanceof ParenthesisExpr) {
			return visitor.visitParenthesisExpr((ParenthesisExpr) this);
		}

		throw new UnsupportedOperationException("Unknown expression type: " + this.getClass().getName());
 }
}