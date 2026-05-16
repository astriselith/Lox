package com.lox.token;

import com.lox.util.Position;
import java.util.HashMap;
import java.util.Map;

public class Token {
 public final Type type;
 public final String lexeme;
 public final Object literal;
 public final Position position;

 private static final Map<String, String> lexemeCache = new HashMap<>();
 private static final Map<Object, Object> literalCache = new HashMap<>();

 private Token(Type type, String lexeme, Object literal, Position position) {
		this.type = type;
		this.lexeme = internLexeme(lexeme);
		this.literal = internLiteral(literal);
		this.position = position;
 }

 public static Token of(
		Type type, String lexeme, Object literal, int line, int column, int start, int end) {
		return new Token(type, lexeme, literal, new Position(line, column, start, end));
 }

 public static Token of(Type type, String lexeme, int line, int column, int start, int end) {
		return of(type, lexeme, null, line, column, start, end);
 }

 public static Token eof(int line, int column, int start, int end) {
		return of(Type.EOF, "", null, line, column, start, end);
 }

 private static String internLexeme(String lexeme) {
		if (lexeme == null)
			return null;
		return lexemeCache.computeIfAbsent(lexeme, l -> l);
 }

 private static Object internLiteral(Object literal) {
		if (literal == null)
			return null;
		return literalCache.computeIfAbsent(literal, l -> l);
 }

 public static void clearCache() {
		lexemeCache.clear();
		literalCache.clear();
 }

 public int getLine() {
		return position.getLine();
 }
 public int getColumn() {
		return position.getColumn();
 }
 public int getStart() {
		return position.getStart();
 }
 public int getEnd() {
		return position.getEnd();
 }
 public Position getPosition() {
		return position;
 }

 public boolean isKeyword() {
		return type == Type.VAR || type == Type.FUN || type == Type.RETURN || type == Type.THROW
			|| type == Type.IF || type == Type.ELSE || type == Type.WHILE || type == Type.BREAK
			|| type == Type.CONTINUE;
 }

 public boolean isOperator() {
		return type == Type.PLUS || type == Type.MINUS || type == Type.STAR || type == Type.SLASH
			|| type == Type.PERCENT || type == Type.QUESTION || type == Type.QUESTION_QUESTION
			|| type == Type.PLUS_PLUS || type == Type.MINUS_MINUS || type == Type.EQ || type == Type.EQ_EQ
			|| type == Type.NOT || type == Type.NOT_EQ || type == Type.LT || type == Type.GT
			|| type == Type.LTE || type == Type.GTE || type == Type.AND || type == Type.AND_AND
			|| type == Type.OR || type == Type.OR_OR || type == Type.PLUS_EQ || type == Type.MINUS_EQ
			|| type == Type.STAR_EQ || type == Type.SLASH_EQ || type == Type.PERCENT_EQ
			|| type == Type.AND_EQ || type == Type.OR_EQ || type == Type.ARROW;
 }

 public boolean isComparison() {
		return type == Type.LT || type == Type.GT || type == Type.LTE || type == Type.GTE
			|| type == Type.EQ_EQ || type == Type.NOT_EQ;
 }

 public boolean isLogical() {
		return type == Type.AND_AND || type == Type.OR_OR;
 }

 public boolean isArithmetic() {
		return type == Type.PLUS || type == Type.MINUS || type == Type.STAR || type == Type.SLASH
			|| type == Type.PERCENT;
 }

 public boolean isIncrementDecrement() {
		return type == Type.PLUS_PLUS || type == Type.MINUS_MINUS;
 }

 public boolean isLiteral() {
		return type == Type.NUMBER || type == Type.STRING || type == Type.BOOLEAN || type == Type.NULL;
 }

 public boolean isPunctuation() {
		return type == Type.LPAREN || type == Type.RPAREN || type == Type.LBRACE || type == Type.RBRACE
			|| type == Type.LBRACKET || type == Type.RBRACKET || type == Type.COMMA || type == Type.DOT
			|| type == Type.SEMICOLON || type == Type.COLON || type == Type.AT;
 }

 public boolean isClosing() {
		return type == Type.RPAREN || type == Type.RBRACE || type == Type.RBRACKET
			|| type == Type.SEMICOLON || type == Type.EOF;
 }

 public boolean isOpening() {
		return type == Type.LPAREN || type == Type.LBRACE || type == Type.LBRACKET;
 }

 public boolean isAssignment() {
		return type == Type.EQ || type == Type.PLUS_EQ || type == Type.MINUS_EQ || type == Type.STAR_EQ
			|| type == Type.SLASH_EQ || type == Type.PERCENT_EQ || type == Type.AND_EQ || type == Type.OR_EQ;
 }

 public boolean isUnary() {
		return type == Type.NOT || type == Type.MINUS;
 }

 public boolean isReturn() {
		return type == Type.RETURN;
 }

 public boolean isThrow() {
		return type == Type.THROW;
 }

 public boolean isBreak() {
		return type == Type.BREAK;
 }

 public boolean isContinue() {
		return type == Type.CONTINUE;
 }

 @Override
 public String toString() {
		return type + " " + lexeme + " " + (literal != null ? literal : "") + " " + position;
 }
}