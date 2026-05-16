package com.lox.parser;

import java.util.ArrayList;
import java.util.List;

import com.lox.token.*;
import com.lox.ast.*;
import com.lox.util.Pair;
import com.lox.util.Position;

public class Parser {
	private static final String ERR_UNEXPECTED = "Token inesperado: %s";
	private static final String ERR_EXPECTED = "Esperado %s, encontrado %s";
	private static final String ERR_VAR = "Esperado 'var'";
	private static final String ERR_FUN = "Esperado 'fun'";
	private static final String ERR_NAME = "Esperado nome";
	private static final String ERR_CONST = "Esperado 'var', 'fun' ou nome após 'const'";
	private static final String ERR_AFTER_ID = "Esperado '(', '=', ';' ou '->' após identificador";
	private static final String ERR_LPAREN = "Esperado '('";
	private static final String ERR_RPAREN = "Esperado ')'";
	private static final String ERR_COLON = "Esperado ':'";
	private static final String ERR_PARAM = "Esperado nome do parâmetro";
	private static final String ERR_VARARGS = "Apenas um varargs é permitido";
	private static final String ERR_VARARGS_LAST = "Varargs deve ser o último";
	private static final String ERR_LBRACE = "Esperado '{'";
	private static final String ERR_RBRACE = "Esperado '}'";
	private static final String ERR_ARROW_BRACE = "Esperado '->' ou '{'";
	private static final String ERR_RBRACKET = "Esperado ']'";
	private static final String ERR_ASSIGN = "Alvo de atribuição inválido";
	private static final String ERR_LBRACKET = "Esperado '['";
	private static final String ERR_IF = "Esperado '(' após 'if'";
	private static final String ERR_WHILE = "Esperado '(' após 'while'";
	private static final String ERR_BREAK_OUTSIDE = "'break' só pode ser usado dentro de loop";
	private static final String ERR_CONTINUE_OUTSIDE = "'continue' só pode ser usado dentro de loop";
	private static final String ERR_EXPECTED_SEMICOLON = "Esperado ';'";
	private static final String ERR_BLOCK_TABLE = "Bloco tabela só pode conter declarações (var, const, fun)";
	private static final String ERR_LAMBDA_ARROW = "Lambda sem parênteses requer '->' antes do corpo";
	private static final String ERR_INVALID_STMT_EXPR = "Expressão não pode ser usada como statement. Apenas atribuições, chamadas de função e incrementos/decrementos são permitidos";
	private static final String ERR_INVALID_UNARY_STMT = "Operador unário '%s' não pode ser usado como statement";

	private final TokenStream stream;
	private FunDeclExpr currentFunction = null;
	private int loopDepth = 0;

	public Parser(TokenStream stream) {
		this.stream = stream;
	}

	public List<Expr> parse() {
		try {
			List<Expr> statements = new ArrayList<>();

			while (!stream.isAtEnd()) {
				Expr e = stmt();
				if (e != null) statements.add(e);
			}

			return statements;
		} catch (ParsingException e) {
			throw e;
		} catch (Exception e) {
			throw new ParsingException(String.format("Erro: %s", e.getMessage()), Position.ZERO, e);
		}
	}

	private Position pos(Token t) {
		return t.getPosition();
	}

	private Position between(Token start, Token end) {
		return Position.between(pos(start), pos(end));
	}

	private Position between(Token start, Position end) {
		return Position.between(pos(start), end);
	}
	
	private Position between(Position start, Token end) {
		return Position.between(start, pos(end));
	}

	private Position between(Position start, Position end) {
		return Position.between(start, end);
	}

	private Position between(List<Expr> expressions) {
		if (expressions == null || expressions.isEmpty()) {
			return Position.ZERO;
		}
		return expressions.get(expressions.size() - 1).getPosition();
	}

	private ParsingException error(String msg) {
		return new ParsingException(msg, pos(stream.peek()));
	}

	private ParsingException error(String msg, Token t) {
		return new ParsingException(msg, pos(t));
	}

	private ParsingException error(String msg, Token start, Token end) {
		return new ParsingException(msg, between(start, end));
	}

	private ParsingException error(String msg, Position p) {
		return new ParsingException(msg, p);
	}

	private ParsingException errorExpected(String expected, Token found) {
		return new ParsingException(String.format(ERR_EXPECTED, expected, found.lexeme), pos(found));
	}

	private ParsingException errorUnexpected(Token t) {
		return new ParsingException(String.format(ERR_UNEXPECTED, t.lexeme), pos(t));
	}

	private void semicolon() {
		if (stream.check(Type.RBRACE)) return;
		if (!stream.match(Type.SEMICOLON)) throw error(ERR_EXPECTED_SEMICOLON);
	}

	private List<Expr> body() {
		if (!stream.match(Type.LBRACE)) throw error(ERR_LBRACE);
		
		List<Expr> statements = new ArrayList<>();

		while (!stream.isAtEnd() && !stream.check(Type.RBRACE)) {
			Expr s = stmt();
			if (s != null) statements.add(s);
		}

		if (!stream.match(Type.RBRACE)) throw error(ERR_RBRACE);

		return statements;
	}

	private Expr block() {
		if (!stream.match(Type.LBRACE)) throw error(ERR_LBRACE);

		Token start = stream.previous();
		List<Expr> declarations = new ArrayList<>();

		while (!stream.isAtEnd() && !stream.check(Type.RBRACE)) {
			if (stream.checkAny(Type.CONST, Type.VAR, Type.FUN)) {
				declarations.add(decl());
			} else {
				throw error(ERR_BLOCK_TABLE, stream.peek());
			}
		}

		if (!stream.match(Type.RBRACE)) throw error(ERR_RBRACE);

		Token end = stream.previous();
		return new BlockExpr(declarations, between(start, end));
	}

	private Expr decl() {
		if (stream.check(Type.CONST)) {
			if (stream.checkNextAny(Type.VAR, Type.FUN)) {
				if (stream.checkNext(Type.VAR)) return varDecl();
				if (stream.checkNext(Type.FUN)) return funDecl();
			}

			if (stream.checkNext(Type.IDENTIFIER)) {
				if (stream.checkNextNextAny(Type.LPAREN, Type.ARROW)) return funDecl();
				if (stream.checkNextNextAny(Type.EQ, Type.SEMICOLON)) return varDecl();
				throw error(ERR_AFTER_ID, stream.peekNext());
			}

			throw error(ERR_CONST, stream.peekNextNext());
		}

		if (stream.check(Type.VAR)) return varDecl();
		if (stream.check(Type.FUN)) return funDecl();

		throw error("Erro interno: decl() chamado sem const, var ou fun");
	}

	private Expr varDecl() {
		Token start = stream.peek();
		boolean isConst = stream.match(Type.CONST);

		if (!stream.match(Type.VAR) && !isConst) throw error(ERR_VAR);

		Token name;
		if (stream.check(Type.IDENTIFIER)) {
			name = stream.advance();
		} else {
			throw error(ERR_NAME);
		}

		Expr value = null;
		if (stream.match(Type.EQ) && !stream.match(Type.QUESTION)) {
			value = expr();
		}

		semicolon();

		return new VarDeclExpr(
				   name.lexeme,
				   value,
				   isConst,
				   between(start, stream.previous())
			   );
	}

	private Expr funDecl() {
		Token start = stream.peek();
		boolean isConst = stream.match(Type.CONST);

		if (!stream.match(Type.FUN) && !isConst) throw error(ERR_FUN);
		if (!stream.check(Type.IDENTIFIER)) throw error(ERR_NAME);

		Token nameToken = stream.advance();
		String name = nameToken.lexeme;

		if (!stream.check(Type.LPAREN)) throw error(ERR_LPAREN);

		Pair<List<String>, Boolean> params = parameters();
		FunDeclExpr prev = currentFunction;

		FunDeclExpr f = new FunDeclExpr(
			name,
			params != null ? params.getFirst() : null,
			new ArrayList<>(),
			isConst,
			params != null && params.getSecond(),
			null
		);

		currentFunction = f;

		if (stream.match(Type.ARROW)) {
			Expr body = expr();
			semicolon();
			List<Expr> bodyList = new ArrayList<>();
			bodyList.add(body);
			f.setBody(bodyList);
			f.setPosition(between(start, body.getPosition()));
		} else if (stream.check(Type.LBRACE)) {
			List<Expr> bodyList = body();
			f.setBody(bodyList);
			f.setPosition(between(
							  start.getPosition(),
							  bodyList.isEmpty() ? start.getPosition() : between(bodyList)
						  ));
		} else {
			throw error(ERR_ARROW_BRACE);
		}

		currentFunction = prev;
		return f;
	}

	private Expr lambdaDecl() {
		Token start = stream.advance();

		boolean hasParens = stream.check(Type.LPAREN);
		Pair<List<String>, Boolean> params;

		if (hasParens) {
			params = parameters();
		} else {
			params = Pair.of(new ArrayList<>(), false);
		}

		FunDeclExpr prev = currentFunction;
		FunDeclExpr lambda = new FunDeclExpr(
			null,
			params.getFirst(),
			new ArrayList<>(),
			false,
			params.getSecond(),
			null
		);

		currentFunction = lambda;
		boolean hasArrow = stream.match(Type.ARROW);

		if (!hasParens && !hasArrow) throw error(ERR_LAMBDA_ARROW);

		if (hasArrow) {
			Expr body = expr();
			List<Expr> bodyList = new ArrayList<>();
			bodyList.add(body);
			lambda.setBody(bodyList);
			lambda.setPosition(between(start, body.getPosition()));
		} else if (stream.check(Type.LBRACE)) {
			List<Expr> bodyList = body();
			lambda.setBody(bodyList);
			lambda.setPosition(between(
								   start.getPosition(),
								   bodyList.isEmpty() ? start.getPosition() : between(bodyList)
							   ));
		} else {
			throw error(ERR_ARROW_BRACE);
		}

		currentFunction = prev;
		return lambda;
	}

	private Expr stmt() {
		Token t = stream.peek();

		if (t.type == Type.CONST || t.type == Type.VAR || t.type == Type.FUN) {
			return decl();
		}

		if (t.type == Type.IF) return ifStmt();
		if (t.type == Type.WHILE) return whileStmt();
		if (t.isBreak()) return breakStmt();
		if (t.isContinue()) return continueStmt();
		if (t.isReturn()) return returnStmt();
		if (t.isThrow()) return throwStmt();

		return exprStmt();
	}

	private Expr ifStmt() {
		Token start = stream.advance();

		if (!stream.match(Type.LPAREN)) throw error(ERR_IF);

		Expr condition = expr();

		if (!stream.match(Type.RPAREN)) throw error(ERR_RPAREN);

		List<Expr> thenBranch;
		if (stream.check(Type.LBRACE)) {
			thenBranch = body();
		} else {
			thenBranch = new ArrayList<>();
			thenBranch.add(stmt());
		}

		List<Expr> elseBranch = null;

		if (stream.match(Type.ELSE)) {
			if (stream.check(Type.LBRACE)) {
				elseBranch = body();
			} else {
				elseBranch = new ArrayList<>();
				elseBranch.add(stmt());
			}
		}

		return new IfExpr(
				   condition,
				   thenBranch,
				   elseBranch,
				   between(start, elseBranch != null ? between(elseBranch) : between(thenBranch))
			   );
	}

	private Expr whileStmt() {
		Token start = stream.advance();

		if (!stream.match(Type.LPAREN)) throw error(ERR_WHILE);

		Expr condition = expr();

		if (!stream.match(Type.RPAREN)) throw error(ERR_RPAREN);

		loopDepth++;

		List<Expr> bodyList;
		if (stream.check(Type.LBRACE)) {
			bodyList = body();
		} else {
			bodyList = new ArrayList<>();
			bodyList.add(stmt());
		}

		loopDepth--;

		return new WhileExpr(condition, bodyList, between(start, between(bodyList)));
	}

	private Expr breakStmt() {
		Token t = stream.advance();
		if (loopDepth == 0) throw error(ERR_BREAK_OUTSIDE, t);
		semicolon();
		return new BreakExpr(pos(t));
	}

	private Expr continueStmt() {
		Token t = stream.advance();
		if (loopDepth == 0) throw error(ERR_CONTINUE_OUTSIDE, t);
		semicolon();
		return new ContinueExpr(pos(t));
	}

	private Expr returnStmt() {
		Token t = stream.advance();

		if (stream.checkAny(Type.SEMICOLON, Type.RBRACE)) {
			semicolon();
			return new ReturnExpr(null, currentFunction, pos(t));
		}

		Expr value = expr();
		semicolon();
		return new ReturnExpr(value, currentFunction, between(t, value.getPosition()));
	}

	private Expr throwStmt() {
		Token t = stream.advance();

		if (stream.checkAny(Type.SEMICOLON, Type.RBRACE)) {
			semicolon();
			return new ThrowExpr(null, currentFunction, pos(t));
		}

		Expr value = expr();
		semicolon();
		return new ThrowExpr(value, currentFunction, between(t, value.getPosition()));
	}

	private Expr exprStmt() {
		Token start = stream.peek();
		Expr e = expr();
		
		if (!(e instanceof AssignExpr ||
				e instanceof SetExpr ||
				e instanceof IndexSetExpr ||
				e instanceof CallExpr ||
				e instanceof UnaryExpr)) {
			throw error(ERR_INVALID_STMT_EXPR, start, stream.previous());
		}

		if (e instanceof UnaryExpr) {
			UnaryExpr unary = (UnaryExpr) e;
			if (!unary.operator.equals("++") && !unary.operator.equals("--")) {
				throw error(String.format(ERR_INVALID_UNARY_STMT, unary.operator), start, stream.previous());
			}
		}

		semicolon();
		return e;
	}

	private Expr expr() {
		return assignment();
	}

	private Expr assignment() {
		Expr expr = logicalOr();
		Token op = stream.peek();

		if (op.isAssignment()) {
			stream.advance();
			Expr value = assignment();

			if (expr instanceof VarExpr) {
				String name = ((VarExpr) expr).name;

				if (op.type != Type.EQ) {
					String binaryOp = op.lexeme.substring(0, op.lexeme.length() - 1);
					Expr binary = new BinaryExpr(expr, binaryOp, value, between(op, value.getPosition()));
					return new AssignExpr(name, binary, between(expr.getPosition(), value.getPosition()));
				}

				return new AssignExpr(name, value, between(expr.getPosition(), value.getPosition()));

			} else if (expr instanceof GetExpr) {
				GetExpr get = (GetExpr) expr;

				if (op.type != Type.EQ) {
					String binaryOp = op.lexeme.substring(0, op.lexeme.length() - 1);
					Expr binary = new BinaryExpr(expr, binaryOp, value, between(op, value.getPosition()));
					return new SetExpr(get.object, get.name, binary, between(expr.getPosition(), value.getPosition()));
				}

				return new SetExpr(get.object, get.name, value, between(expr.getPosition(), value.getPosition()));

			} else if (expr instanceof IndexGetExpr) {
				IndexGetExpr idx = (IndexGetExpr) expr;

				if (op.type != Type.EQ) {
					String binaryOp = op.lexeme.substring(0, op.lexeme.length() - 1);
					Expr binary = new BinaryExpr(expr, binaryOp, value, between(op, value.getPosition()));
					return new IndexSetExpr(idx.array, idx.index, binary, between(expr.getPosition(), value.getPosition()));
				}

				return new IndexSetExpr(idx.array, idx.index, value, between(expr.getPosition(), value.getPosition()));
			}

			throw error(ERR_ASSIGN, op);
		}

		return expr;
	}

	private Expr logicalOr() {
		Expr expr = logicalAnd();

		while (stream.match(Type.OR_OR)) {
			Token op = stream.previous();
			expr = new LogicalExpr(expr, op.lexeme, logicalAnd(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr logicalAnd() {
		Expr expr = coalesce();

		while (stream.match(Type.AND_AND)) {
			Token op = stream.previous();
			expr = new LogicalExpr(expr, op.lexeme, coalesce(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr coalesce() {
		Expr expr = ternary();

		while (stream.match(Type.QUESTION_QUESTION)) {
			Token op = stream.previous();
			Expr right = ternary();
			expr = new BinaryExpr(expr, op.lexeme, right, between(expr.getPosition(), right.getPosition()));
		}

		return expr;
	}

	private Expr ternary() {
		Expr expr = equality();

		if (stream.match(Type.QUESTION)) {
			Expr thenExpr = expr();
			if (!stream.match(Type.COLON)) throw error(ERR_COLON);
			Expr elseExpr = expr();
			return new TernaryExpr(expr, thenExpr, elseExpr, between(expr.getPosition(), elseExpr.getPosition()));
		}

		return expr;
	}

	private Expr equality() {
		Expr expr = comparison();

		while (stream.matchAny(Type.EQ_EQ, Type.NOT_EQ)) {
			Token op = stream.previous();
			expr = new BinaryExpr(expr, op.lexeme, comparison(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr comparison() {
		Expr expr = term();

		while (stream.matchAny(Type.LT, Type.GT, Type.LTE, Type.GTE)) {
			Token op = stream.previous();
			expr = new BinaryExpr(expr, op.lexeme, term(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr term() {
		Expr expr = factor();

		while (stream.matchAny(Type.PLUS, Type.MINUS)) {
			Token op = stream.previous();
			expr = new BinaryExpr(expr, op.lexeme, factor(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr factor() {
		Expr expr = unary();

		while (stream.matchAny(Type.STAR, Type.SLASH, Type.PERCENT)) {
			Token op = stream.previous();
			expr = new BinaryExpr(expr, op.lexeme, unary(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr unary() {
		Token t = stream.peek();

		if (t.isUnary()) {
			stream.advance();
			return new UnaryExpr(t.lexeme, unary(), UnaryExpr.PREFIX, between(t, stream.previous()));
		}

		if (t.isIncrementDecrement()) {
			stream.advance();
			return new UnaryExpr(t.lexeme, unary(), UnaryExpr.PREFIX, between(t, stream.previous()));
		}

		return call();
	}

	private Expr call() {
		Expr expr = primary();

		while (true) {
			if (stream.match(Type.DOT)) {
				Token dot = stream.previous();
				if (!stream.check(Type.IDENTIFIER)) throw error(ERR_NAME);
				Token name = stream.advance();
				expr = new GetExpr(expr, name.lexeme, between(dot, name));
				continue;
			}

			if (stream.match(Type.LBRACKET)) {
				Token bracket = stream.previous();
				Expr idx = expr();
				if (!stream.check(Type.RBRACKET)) throw error(ERR_RBRACKET);
				Token rbracket = stream.advance();
				expr = new IndexGetExpr(expr, idx, between(bracket, rbracket));
				continue;
			}

			if (stream.check(Type.LPAREN)) {
				List<Expr> args = arguments();
				expr = new CallExpr(
					expr,
					args,
					between(expr.getPosition(), !args.isEmpty() ? between(args) : expr.getPosition())
				);
				continue;
			}

			if (stream.peek().isIncrementDecrement()) {
				Token op = stream.advance();
				expr = new UnaryExpr(op.lexeme, expr, UnaryExpr.POSTFIX, between(expr.getPosition(), op));
				break;
			}

			break;
		}

		return expr;
	}

	private List<Expr> arguments() {
		if (!stream.match(Type.LPAREN)) throw error(ERR_LPAREN);

		List<Expr> args = new ArrayList<>();

		if (!stream.check(Type.RPAREN)) {
			do {
				args.add(expr());
			} while (stream.match(Type.COMMA));
		}

		if (!stream.match(Type.RPAREN)) throw error(ERR_RPAREN);

		return args;
	}

	private Pair<List<String>, Boolean> parameters() {
		if (!stream.match(Type.LPAREN)) throw error(ERR_LPAREN);

		List<String> params = new ArrayList<>();
		boolean hasVarargs = false;

		if (!stream.check(Type.RPAREN)) {
			do {
				boolean isVararg = false;
				
				if (stream.check(Type.ELLIPSIS)) {
					if (hasVarargs) throw error(ERR_VARARGS);
					isVararg = true;
					hasVarargs = true;
					stream.advance();
				}

				if (!stream.check(Type.IDENTIFIER)) throw error(ERR_PARAM);

				Token p = stream.advance();
				params.add(p.lexeme);

				if (isVararg) {
					if (!stream.checkAny(Type.RPAREN, Type.COMMA)) throw error(ERR_VARARGS_LAST, p);
					if (stream.check(Type.COMMA)) throw error(ERR_VARARGS_LAST, p);
				}
			} while (stream.match(Type.COMMA));
		}

		if (!stream.match(Type.RPAREN)) throw error(ERR_RPAREN);

		return Pair.of(params, hasVarargs);
	}

	private Expr array() {
		if (!stream.match(Type.LBRACKET)) throw error(ERR_LBRACKET);

		Token start = stream.previous();
		List<Expr> elements = new ArrayList<>();

		if (!stream.check(Type.RBRACKET)) {
			do {
				elements.add(expr());
			} while (stream.match(Type.COMMA));
		}

		if (!stream.match(Type.RBRACKET)) throw error(ERR_RBRACKET);

		Token end = stream.previous();
		return new ArrayExpr(elements, between(start, end));
	}

	private Expr parenthesis() {
		if (!stream.match(Type.LPAREN)) throw error(ERR_LPAREN);

		Token start = stream.previous();
		Expr inner = expr();

		if (!stream.match(Type.RPAREN)) throw error(ERR_RPAREN);

		Token end = stream.previous();
		return new ParenthesisExpr(inner, between(start, end));
	}

	private Expr primary() {
		if (stream.peek().isLiteral()) {
			Token t = stream.advance();
			return new LiteralExpr(t.literal, pos(t));
		}

		if (stream.match(Type.IDENTIFIER)) {
			return new VarExpr(stream.previous().lexeme, pos(stream.previous()));
		}

		if (stream.check(Type.FUN)) return lambdaDecl();
		if (stream.check(Type.LBRACKET)) return array();
		if (stream.check(Type.LBRACE)) return block();
		if (stream.check(Type.LPAREN)) return parenthesis();

		throw errorUnexpected(stream.peek());
	}
}