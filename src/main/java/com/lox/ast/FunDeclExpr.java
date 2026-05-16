package com.lox.ast;

import com.lox.util.Position;
import java.util.List;

public class FunDeclExpr extends Expr {
	public final String name;
	public final boolean anonymous;
	public final List<String> parameters;
	public final boolean hasVarargs;
	private List<Expr> body;

	public FunDeclExpr(String name, List<String> parameters, List<Expr> body,
					   boolean hasVarargs, Position position) {
		super(position);
		if (name != null) {
			this.name = name;
			this.anonymous = false;
		} else {
			this.name = "";
			this.anonymous = true;
		}
		this.parameters = parameters;
		this.body = body;
		this.hasVarargs = hasVarargs;
	}

	public List<Expr> getBody() {
		return body;
	}

	public void setBody(List<Expr> body) {
		this.body = body;
	}
}