package com.lox.parser;

import java.util.ArrayList;
import java.util.List;

import com.lox.token.*;
import com.lox.ast.*;
import com.lox.util.Pair;
import com.lox.util.Position;
import static com.lox.token.Type.*;

public class Parser {
	// Erros de parsing
	private static final String ERR_UNEXPECTED = "Token inesperado: %s";
	private static final String ERR_EXPECTED = "Esperado %s, encontrado %s";
	private static final String ERR_VAR = "Esperado 'var'";
	private static final String ERR_FUN = "Esperado 'fun'";
	private static final String ERR_NAME = "Esperado nome";
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
	private static final String ERR_BLOCK_TABLE = "Bloco tabela só pode conter declarações (var, fun)";
	private static final String ERR_LAMBDA_ARROW = "Lambda sem parênteses requer '->' antes do corpo";
	private static final String ERR_INVALID_STMT_EXPR = "Expressão não pode ser usada como statement. Apenas atribuições, chamadas de função e incrementos/decrementos são permitidos";
	private static final String ERR_INVALID_UNARY_STMT = "Operador unário '%s' não pode ser usado como statement";
	private static final String ERR_DECOR_NAME = "Esperado nome do decorator após '@'";
	private static final String ERR_DECOR_FUNCTION = "Funções não podem ter decoradores";
	private static final String ERR_DECOR_AT = "Esperado '@'";
	private static final String ERR_DECOR_VAR = "Esperado 'var' após decorator";
	private static final String ERR_ARROW_RETURN = "Return statement não permitido em arrow function. O valor é implícito";
	private static final String ERR_ARROW_BREAK_CONTINUE = "Break/continue não permitido em arrow function";

	private final TokenStream stream;
	private FunDeclExpr currentFunction = null;
	private int loopDepth = 0;
	private boolean inVarDecl = false;

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
		if (stream.check(RBRACE)) return;
		if (!stream.match(SEMICOLON)) throw error(ERR_EXPECTED_SEMICOLON);
	}

	private List<Expr> body() {
		if (!stream.match(LBRACE)) throw error(ERR_LBRACE);

		List<Expr> statements = new ArrayList<>();

		while (!stream.isAtEnd() && !stream.check(RBRACE)) {
			Expr s = stmt();
			if (s != null) statements.add(s);
		}

		if (!stream.match(RBRACE)) throw error(ERR_RBRACE);

		return statements;
	}

	private Expr object() {
		if (!stream.match(LBRACE)) throw error(ERR_LBRACE);

		Token start = stream.previous();
		List<Expr> declarations = new ArrayList<>();

		while (!stream.isAtEnd() && !stream.check(RBRACE)) {
			if (stream.checkAny(VAR, FUN)) {
				declarations.add(decl());
			} else {
				throw error(ERR_BLOCK_TABLE, stream.peek());
			}
		}

		if (!stream.match(RBRACE)) throw error(ERR_RBRACE);

		Token end = stream.previous();
		return new ObjectExpr(declarations, between(start, end));
	}

	private DecorExpr decor() {
		if (!stream.match(AT)) throw error(ERR_DECOR_AT);

		Token atToken = stream.previous();

		if (!stream.check(IDENTIFIER)) {
			throw error(ERR_DECOR_NAME, atToken);
		}

		Token nameToken = stream.advance();
		List<Expr> args = null;

		if (stream.check(LPAREN)) {
			args = arguments();
		}

		return new DecorExpr(nameToken.lexeme, args, between(atToken, nameToken));
	}

	private Expr decl() {
		List<DecorExpr> decorators = new ArrayList<>();

		while (stream.check(AT)) {
			decorators.add(decor());
		}

		if (stream.check(VAR)) return varDecl(decorators);
		if (stream.check(FUN)) {
			if (!decorators.isEmpty()) {
				throw error(ERR_DECOR_FUNCTION, stream.peek());
			}
			return funDecl();
		}

		if (!decorators.isEmpty()) {
			throw error(ERR_DECOR_VAR);
		}

		throw error("Erro interno: decl() chamado sem var ou fun");
	}

	private Expr varDecl(List<DecorExpr> decorators) {
		Token start = stream.peek();

		if (!stream.match(VAR)) throw error(ERR_VAR);

		Token name;
		if (stream.check(IDENTIFIER)) {
			name = stream.advance();
		} else {
			throw error(ERR_NAME);
		}

		Expr value = null;
		if (stream.match(EQ) && !stream.match(QUESTION)) {
			inVarDecl = true;
			value = expr();
			inVarDecl = false;
		}

		semicolon();

		return new VarDeclExpr(
				   name.lexeme,
				   value,
				   decorators,
				   between(start, stream.previous())
			   );
	}

	private Expr funDecl() {
		Token start = stream.peek();

		if (!stream.match(FUN)) throw error(ERR_FUN);
		if (!stream.check(IDENTIFIER)) throw error(ERR_NAME);

		Token nameToken = stream.advance();
		String name = nameToken.lexeme;

		if (!stream.check(LPAREN)) throw error(ERR_LPAREN);

		Pair<List<String>, Boolean> params = parameters();
		FunDeclExpr prev = currentFunction;

		FunDeclExpr f = new FunDeclExpr(
			name,
			params != null ? params.getFirst() : null,
			null,
			params != null && params.getSecond(),
			null
		);

		currentFunction = f;

		if (stream.match(ARROW)) {
			Expr bodyExpr = arrow();
			semicolon();
			List<Expr> bodyList = new ArrayList<>();
			bodyList.add(bodyExpr);
			f.setBody(bodyList);
			f.setPosition(between(start, bodyExpr.getPosition()));
		} else if (stream.check(LBRACE)) {
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

		boolean hasParens = stream.check(LPAREN);
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
			params.getSecond(),
			null
		);

		currentFunction = lambda;
		boolean hasArrow = stream.match(ARROW);

		if (!hasParens && !hasArrow) throw error(ERR_LAMBDA_ARROW);

		if (hasArrow) {
			Expr bodyExpr = arrow();
			List<Expr> bodyList = new ArrayList<>();
			bodyList.add(bodyExpr);
			lambda.setBody(bodyList);
			lambda.setPosition(between(start, bodyExpr.getPosition()));
		} else if (stream.check(LBRACE)) {
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

	private Expr arrow() {
		if (stream.check(LBRACE)) {
			Expr table = object();
			return new ReturnExpr(table, currentFunction, table.getPosition());
		} else {
			Expr expr = expr();
			
			if (expr instanceof ReturnExpr) {
				throw error(ERR_ARROW_RETURN);
			}
			if (expr instanceof BreakExpr || expr instanceof ContinueExpr) {
				throw error(ERR_ARROW_BREAK_CONTINUE);
			}
			
			return new ReturnExpr(expr, currentFunction, expr.getPosition());
		}
	}

	private Expr stmt() {
		Token t = stream.peek();

		if (t.type == AT || t.type == VAR || t.type == FUN) {
			return decl();
		}

		if (t.type == IF) return ifStmt();
		if (t.type == WHILE) return whileStmt();
		if (t.isBreak()) return breakStmt();
		if (t.isContinue()) return continueStmt();
		if (t.isReturn()) return returnStmt();
		if (t.isThrow()) return throwStmt();

		return exprStmt();
	}

	private Expr ifStmt() {
		Token start = stream.advance();

		if (!stream.match(LPAREN)) throw error(ERR_IF);

		Expr condition = expr();

		if (!stream.match(RPAREN)) throw error(ERR_RPAREN);

		List<Expr> thenBranch;
		if (stream.check(LBRACE)) {
			thenBranch = body();
		} else {
			thenBranch = new ArrayList<>();
			thenBranch.add(stmt());
		}

		List<Expr> elseBranch = null;

		if (stream.match(ELSE)) {
			if (stream.check(LBRACE)) {
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

		if (!stream.match(LPAREN)) throw error(ERR_WHILE);

		Expr condition = expr();

		if (!stream.match(RPAREN)) throw error(ERR_RPAREN);

		loopDepth++;

		List<Expr> bodyList;
		if (stream.check(LBRACE)) {
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

		if (stream.checkAny(SEMICOLON, RBRACE)) {
			semicolon();
			return new ReturnExpr(null, currentFunction, pos(t));
		}

		Expr value = expr();
		semicolon();
		return new ReturnExpr(value, currentFunction, between(t, value.getPosition()));
	}

	private Expr throwStmt() {
		Token t = stream.advance();

		if (stream.checkAny(SEMICOLON, RBRACE)) {
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

				if (op.type != EQ) {
					String binaryOp = op.lexeme.substring(0, op.lexeme.length() - 1);
					Expr binary = new BinaryExpr(expr, binaryOp, value, between(op, value.getPosition()));
					return new AssignExpr(name, binary, between(expr.getPosition(), value.getPosition()));
				}

				return new AssignExpr(name, value, between(expr.getPosition(), value.getPosition()));

			} else if (expr instanceof GetExpr) {
				GetExpr get = (GetExpr) expr;

				if (op.type != EQ) {
					String binaryOp = op.lexeme.substring(0, op.lexeme.length() - 1);
					Expr binary = new BinaryExpr(expr, binaryOp, value, between(op, value.getPosition()));
					return new SetExpr(get.object, get.name, binary, between(expr.getPosition(), value.getPosition()));
				}

				return new SetExpr(get.object, get.name, value, between(expr.getPosition(), value.getPosition()));

			} else if (expr instanceof IndexGetExpr) {
				IndexGetExpr idx = (IndexGetExpr) expr;

				if (op.type != EQ) {
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

		while (stream.match(OR_OR)) {
			Token op = stream.previous();
			expr = new LogicalExpr(expr, op.lexeme, logicalAnd(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr logicalAnd() {
		Expr expr = coalesce();

		while (stream.match(AND_AND)) {
			Token op = stream.previous();
			expr = new LogicalExpr(expr, op.lexeme, coalesce(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr coalesce() {
		Expr expr = ternary();

		while (stream.match(QUESTION_QUESTION)) {
			Token op = stream.previous();
			Expr right = ternary();
			expr = new BinaryExpr(expr, op.lexeme, right, between(expr.getPosition(), right.getPosition()));
		}

		return expr;
	}

	private Expr ternary() {
		Expr expr = equality();

		if (stream.match(QUESTION)) {
			Expr thenExpr = expr();
			if (!stream.match(COLON)) throw error(ERR_COLON);
			Expr elseExpr = expr();
			return new TernaryExpr(expr, thenExpr, elseExpr, between(expr.getPosition(), elseExpr.getPosition()));
		}

		return expr;
	}

	private Expr equality() {
		Expr expr = comparison();

		while (stream.matchAny(EQ_EQ, NOT_EQ)) {
			Token op = stream.previous();
			expr = new BinaryExpr(expr, op.lexeme, comparison(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr comparison() {
		Expr expr = term();

		while (stream.matchAny(LT, GT, LTE, GTE)) {
			Token op = stream.previous();
			expr = new BinaryExpr(expr, op.lexeme, term(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr term() {
		Expr expr = factor();

		while (stream.matchAny(PLUS, MINUS)) {
			Token op = stream.previous();
			expr = new BinaryExpr(expr, op.lexeme, factor(), between(expr.getPosition(), stream.previous().getPosition()));
		}

		return expr;
	}

	private Expr factor() {
		Expr expr = unary();

		while (stream.matchAny(STAR, SLASH, PERCENT)) {
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
			if (stream.match(DOT)) {
				Token dot = stream.previous();
				if (!stream.check(IDENTIFIER)) throw error(ERR_NAME);
				Token name = stream.advance();
				expr = new GetExpr(expr, name.lexeme, between(dot, name));
				continue;
			}

			if (stream.match(LBRACKET)) {
				Token bracket = stream.previous();
				Expr idx = expr();
				if (!stream.check(RBRACKET)) throw error(ERR_RBRACKET);
				Token rbracket = stream.advance();
				expr = new IndexGetExpr(expr, idx, between(bracket, rbracket));
				continue;
			}

			if (stream.check(LPAREN)) {
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
		if (!stream.match(LPAREN)) throw error(ERR_LPAREN);

		List<Expr> args = new ArrayList<>();

		if (!stream.check(RPAREN)) {
			do {
				args.add(expr());
			} while (stream.match(COMMA));
		}

		if (!stream.match(RPAREN)) throw error(ERR_RPAREN);

		return args;
	}

	private Pair<List<String>, Boolean> parameters() {
		if (!stream.match(LPAREN)) throw error(ERR_LPAREN);

		List<String> params = new ArrayList<>();
		boolean hasVarargs = false;

		if (!stream.check(RPAREN)) {
			do {
				boolean isVararg = false;

				if (stream.check(ELLIPSIS)) {
					if (hasVarargs) throw error(ERR_VARARGS);
					isVararg = true;
					hasVarargs = true;
					stream.advance();
				}

				if (!stream.check(IDENTIFIER)) throw error(ERR_PARAM);

				Token p = stream.advance();
				params.add(p.lexeme);

				if (isVararg) {
					if (!stream.checkAny(RPAREN, COMMA)) throw error(ERR_VARARGS_LAST, p);
					if (stream.check(COMMA)) throw error(ERR_VARARGS_LAST, p);
				}
			} while (stream.match(COMMA));
		}

		if (!stream.match(RPAREN)) throw error(ERR_RPAREN);

		return Pair.of(params, hasVarargs);
	}

	private Expr array() {
		if (!stream.match(LBRACKET)) throw error(ERR_LBRACKET);

		Token start = stream.previous();
		List<Expr> elements = new ArrayList<>();

		if (!stream.check(RBRACKET)) {
			do {
				elements.add(expr());
			} while (stream.match(COMMA));
		}

		if (!stream.match(RBRACKET)) throw error(ERR_RBRACKET);

		Token end = stream.previous();
		return new ArrayExpr(elements, between(start, end));
	}

	private Expr parenthesis() {
		if (!stream.match(LPAREN)) throw error(ERR_LPAREN);

		Token start = stream.previous();
		Expr inner = expr();

		if (!stream.match(RPAREN)) throw error(ERR_RPAREN);

		Token end = stream.previous();
		return new ParenthesisExpr(inner, between(start, end));
	}

	private Expr primary() {
		if (stream.peek().isLiteral()) {
			Token t = stream.advance();
			return new LiteralExpr(t.literal, pos(t));
		}

		if (stream.match(IDENTIFIER)) {
			return new VarExpr(stream.previous().lexeme, pos(stream.previous()));
		}

		if (stream.check(FUN)) return lambdaDecl();
		if (stream.check(LBRACKET)) return array();
		if (stream.check(LBRACE)) return object();
		if (stream.check(LPAREN)) return parenthesis();

		throw errorUnexpected(stream.peek());
	}
}