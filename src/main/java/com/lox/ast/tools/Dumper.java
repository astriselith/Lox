package com.lox.ast.tools;

import com.lox.ast.*;
import com.lox.util.Position;
import java.util.*;

public class Dumper implements ExprVisitor<String> {
 private int indent = 0;
 private boolean showPositions = true;
 private boolean showTypes = true;

 public Dumper() {
		this(true, true);
 }

 public Dumper(boolean showPositions, boolean showTypes) {
		this.showPositions = showPositions;
		this.showTypes = showTypes;
 }

 private String indentString() {
		return "  ".repeat(indent);
 }

 private String positionString(Position pos) {
		if (!showPositions || pos == null)
			return "";
		return " [" + pos.getLine() + ":" + pos.getColumn() + "]";
 }

 private String typeString(Object obj) {
		if (!showTypes)
			return "";
		if (obj == null)
			return " (null)";
		String type = obj.getClass().getSimpleName();
		return " <" + type + ">";
 }

 public String dump(Expr expr) {
		return expr.accept(this);
 }

 private void dumpList(List<Expr> expressions, StringBuilder sb, String name) {
		if (expressions == null || expressions.isEmpty()) {
			sb.append("[]\n");
			return;
		}

		sb.append(name).append(": ");
		if (expressions.size() == 1) {
			sb.append("\n");
			indent++;
			sb.append(expressions.get(0).accept(this));
			sb.append("\n");
			indent--;
		} else {
			sb.append("[\n");
			indent++;
			for (Expr e : expressions) {
				sb.append(e.accept(this)).append("\n");
			}
			indent--;
			sb.append(indentString()).append("]\n");
		}
 }

 @Override
 public String visitLiteralExpr(LiteralExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("LiteralExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": ");
		if (expr.value == null) {
			sb.append("null");
		} else if (expr.value instanceof String) {
			sb.append("\"").append(expr.value).append("\"");
		} else {
			sb.append(expr.value);
		}
		return sb.toString();
 }

 @Override
 public String visitVarExpr(VarExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("VarExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": ").append(expr.name);
		return sb.toString();
 }

 @Override
 public String visitAssignExpr(AssignExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("AssignExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");
		indent++;
		sb.append(indentString()).append("name: ").append(expr.name).append("\n");
		sb.append(indentString()).append("value:\n");
		sb.append(expr.value.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitVarDeclExpr(VarDeclExpr expr) {
		StringBuilder sb = new StringBuilder();
		for (Expr d : expr.decorators) {
			sb.append(d.accept(this)).append("\n");
			sb.append(indentString());
		}
		sb.append(indentString()).append("VarDeclExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": var ").append(expr.name);
		if (expr.value != null) {
			sb.append("\n");
			indent++;
			sb.append(indentString()).append("value:\n");
			sb.append(expr.value.accept(this));
			indent--;
		}
		return sb.toString();
 }

 @Override
 public String visitFunDeclExpr(FunDeclExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("FunDeclExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": fun ");
		sb.append(expr.name.isEmpty() ? "<anonymous>" : expr.name);
		sb.append("\n");

		indent++;
		if (expr.parameters != null && !expr.parameters.isEmpty()) {
			sb.append(indentString()).append("parameters: [");
			for (int i = 0; i < expr.parameters.size(); i++) {
				if (i > 0)
					sb.append(", ");
				if (expr.hasVarargs && i == expr.parameters.size() - 1)
					sb.append("...");
				sb.append(expr.parameters.get(i));
			}
			sb.append("]\n");
		}

		dumpList(expr.getBody(), sb, "body");
		indent--;
		return sb.toString();
 }

 @Override
 public String visitDecorExpr(DecorExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("DecorExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": @").append(expr.name);
		if (expr.arguments != null && !expr.arguments.isEmpty()) {
			sb.append("(");
			for (int i = 0; i < expr.arguments.size(); i++) {
				if (i > 0)
					sb.append(", ");
				sb.append(expr.arguments.get(i).accept(this));
			}
			sb.append(")");
		}
		return sb.toString();
 }

 @Override
 public String visitObjectExpr(ObjectExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("ObjectExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		dumpList(expr.declarations, sb, "expressions");
		indent--;
		return sb.toString();
 }

 @Override
 public String visitIfExpr(IfExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("IfExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		sb.append(indentString()).append("condition:\n");
		sb.append(expr.condition.accept(this)).append("\n");
		dumpList(expr.thenBranch, sb, "then");
		if (expr.elseBranch != null) {
			dumpList(expr.elseBranch, sb, "else");
		}
		indent--;
		return sb.toString();
 }

 @Override
 public String visitWhileExpr(WhileExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("WhileExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		sb.append(indentString()).append("condition:\n");
		sb.append(expr.condition.accept(this)).append("\n");
		dumpList(expr.body, sb, "body");
		indent--;
		return sb.toString();
 }

 @Override
 public String visitBreakExpr(BreakExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("BreakExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		return sb.toString();
 }

 @Override
 public String visitContinueExpr(ContinueExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("ContinueExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		return sb.toString();
 }

 @Override
 public String visitBinaryExpr(BinaryExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("BinaryExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": ").append(expr.operator).append("\n");

		indent++;
		sb.append(indentString()).append("left:\n");
		sb.append(expr.left.accept(this)).append("\n");
		sb.append(indentString()).append("right:\n");
		sb.append(expr.right.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitUnaryExpr(UnaryExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("UnaryExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": ").append(expr.operator);
		if (expr.isPrefix()) {
			sb.append("(prefix)");
		} else if (expr.isPostfix()) {
			sb.append("(postfix)");
		}
		sb.append("\n");

		indent++;
		sb.append(indentString()).append("operand:\n");
		sb.append(expr.operand.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitLogicalExpr(LogicalExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("LogicalExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": ").append(expr.operator).append("\n");

		indent++;
		sb.append(indentString()).append("left:\n");
		sb.append(expr.left.accept(this)).append("\n");
		sb.append(indentString()).append("right:\n");
		sb.append(expr.right.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitTernaryExpr(TernaryExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("TernaryExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		sb.append(indentString()).append("condition:\n");
		sb.append(expr.condition.accept(this)).append("\n");
		sb.append(indentString()).append("then:\n");
		sb.append(expr.thenExpr.accept(this)).append("\n");
		sb.append(indentString()).append("else:\n");
		sb.append(expr.elseExpr.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitCallExpr(CallExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("CallExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		sb.append(indentString()).append("callee:\n");
		sb.append(expr.callee.accept(this)).append("\n");
		dumpList(expr.arguments, sb, "arguments");
		indent--;
		return sb.toString();
 }

 @Override
 public String visitGetExpr(GetExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("GetExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": ").append(expr.name).append("\n");

		indent++;
		sb.append(indentString()).append("object:\n");
		sb.append(expr.object.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitSetExpr(SetExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("SetExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append(": ").append(expr.name).append("\n");

		indent++;
		sb.append(indentString()).append("object:\n");
		sb.append(expr.object.accept(this)).append("\n");
		sb.append(indentString()).append("value:\n");
		sb.append(expr.value.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitIndexGetExpr(IndexGetExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("IndexGetExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		sb.append(indentString()).append("array:\n");
		sb.append(expr.array.accept(this)).append("\n");
		sb.append(indentString()).append("index:\n");
		sb.append(expr.index.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitIndexSetExpr(IndexSetExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("IndexSetExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		sb.append(indentString()).append("array:\n");
		sb.append(expr.array.accept(this)).append("\n");
		sb.append(indentString()).append("index:\n");
		sb.append(expr.index.accept(this)).append("\n");
		sb.append(indentString()).append("value:\n");
		sb.append(expr.value.accept(this));
		indent--;
		return sb.toString();
 }

 @Override
 public String visitArrayExpr(ArrayExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("ArrayExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		dumpList(expr.elements, sb, "elements");
		indent--;
		return sb.toString();
 }

 @Override
 public String visitReturnExpr(ReturnExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("ReturnExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		if (expr.enclosingFunction != null) {
			sb.append(" -> ").append(
				expr.enclosingFunction.name.isEmpty() ? "<anonymous>" : expr.enclosingFunction.name);
		}
		if (expr.value != null) {
			sb.append("\n");
			indent++;
			sb.append(indentString()).append("value:\n");
			sb.append(expr.value.accept(this));
			indent--;
		}
		return sb.toString();
 }

 @Override
 public String visitThrowExpr(ThrowExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("ThrowExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		if (expr.enclosingFunction != null) {
			sb.append(" -> ").append(
				expr.enclosingFunction.name.isEmpty() ? "<anonymous>" : expr.enclosingFunction.name);
		}
		if (expr.value != null) {
			sb.append("\n");
			indent++;
			sb.append(indentString()).append("value:\n");
			sb.append(expr.value.accept(this));
			indent--;
		}
		return sb.toString();
 }

 @Override
 public String visitParenthesisExpr(ParenthesisExpr expr) {
		StringBuilder sb = new StringBuilder();
		sb.append(indentString()).append("ParenthesisExpr");
		sb.append(positionString(expr.getPosition()));
		sb.append(typeString(expr));
		sb.append("\n");

		indent++;
		sb.append(indentString()).append("expression:\n");
		sb.append(expr.expression.accept(this));
		indent--;
		return sb.toString();
 }

 public void setShowPositions(boolean show) {
		this.showPositions = show;
 }

 public void setShowTypes(boolean show) {
		this.showTypes = show;
 }
}