package com.lox.lexer;

import java.util.HashMap;
import java.util.Map;

import com.lox.token.*;
import com.lox.util.Pair;
import com.lox.util.Position;

public class Lexer extends TokenStream {
	private final String source;
	private int current = 0;
	private int line = 1;
	private int lineStart = 0;
	private int start = 0;
	private boolean eofReached = false;

	private static final Map<String, Pair<Type, String>> keywords = new HashMap<>();

	static {
		keywords.put("var", Pair.of(Type.VAR, "var"));
		keywords.put("fun", Pair.of(Type.FUN, "fun"));
		keywords.put("const", Pair.of(Type.CONST, "const"));
		keywords.put("if", Pair.of(Type.IF, "if"));
		keywords.put("else", Pair.of(Type.ELSE, "else"));
		keywords.put("while", Pair.of(Type.WHILE, "while"));
		keywords.put("break", Pair.of(Type.BREAK, "break"));
		keywords.put("continue", Pair.of(Type.CONTINUE, "continue"));
		keywords.put("return", Pair.of(Type.RETURN, "return"));
		keywords.put("throw", Pair.of(Type.THROW, "throw"));
		keywords.put("true", Pair.of(Type.BOOLEAN, "true"));
		keywords.put("false", Pair.of(Type.BOOLEAN, "false"));
		keywords.put("null", Pair.of(Type.NULL, "null"));

		keywords.put("let", Pair.of(Type.VAR, "var"));
		keywords.put("func", Pair.of(Type.FUN, "fun"));
		keywords.put("function", Pair.of(Type.FUN, "fun"));
		keywords.put("constant", Pair.of(Type.CONST, "const"));
		keywords.put("nil", Pair.of(Type.NULL, "null"));
		keywords.put("none", Pair.of(Type.NULL, "null"));

		keywords.put("and", Pair.of(Type.AND_AND, "&&"));
		keywords.put("or", Pair.of(Type.OR_OR, "||"));
		keywords.put("not", Pair.of(Type.NOT, "!"));

		keywords.put("eq", Pair.of(Type.EQ_EQ, "=="));
		keywords.put("neq", Pair.of(Type.NOT_EQ, "!="));
		keywords.put("lt", Pair.of(Type.LT, "<"));
		keywords.put("gt", Pair.of(Type.GT, ">"));
		keywords.put("lte", Pair.of(Type.LTE, "<="));
		keywords.put("gte", Pair.of(Type.GTE, ">="));

		keywords.put("inc", Pair.of(Type.PLUS_PLUS, "++"));
		keywords.put("dec", Pair.of(Type.MINUS_MINUS, "--"));
	}

	public Lexer(String source) {
		super();
		this.source = source;
	}

	@Override
	protected Token fetchNextToken() {
		try {
			if (eofReached) {
				int column = current - lineStart + 1;
				return Token.eof(line, column, current, current);
			}

			// Pula whitespace e comentários antes de cada token
			skipWhitespace();

			if (isAtEndChar()) {
				eofReached = true;
				int column = current - lineStart + 1;
				return Token.eof(line, column, current, current);
			}

			start = current;
			Token token = scan();
			return token;
		} catch (LexingException e) {
			throw e;
		} catch (Exception e) {
			throw new LexingException("Erro durante lexing: " + e.getMessage(), getCurrentPosition(), e);
		}
	}

	private void skipWhitespace() {
		while (!isAtEndChar()) {
			char c = peekChar();

			switch (c) {
			case ' ':
			case '\r':
			case '\t':
				advanceChar();
				break;

			case '\n':
				advanceChar();
				line++;
				lineStart = current;
				break;

			case '/':
				if (peekNextChar() == '/') {
					// Comentário de linha
					while (!isAtEndChar() && peekChar() != '\n') {
						advanceChar();
					}
				} else if (peekNextChar() == '*') {
					// Comentário de múltiplas linhas
					advanceChar();
					advanceChar();

					while (!isAtEndChar()) {
						if (peekChar() == '\n') {
							advanceChar();
							line++;
							lineStart = current;
							continue;
						}

						if (peekChar() == '*' && peekNextChar() == '/') {
							advanceChar();
							advanceChar();
							break;
						}

						advanceChar();
					}
				} else {
					return;
				}
				break;

			default:
				return;
			}
		}
	}

	private Position getCurrentPosition() {
		int column = start - lineStart + 1;
		return new Position(line, column, start, current);
	}

	private Token scan() {
		char c = advanceChar();

		switch (c) {
		case '+':
			if (matchChar('+')) {
				return token(Type.PLUS_PLUS, "++");
			} else if (matchChar('=')) {
				return token(Type.PLUS_EQ, "+=");
			} else {
				return token(Type.PLUS, "+");
			}

		case '-':
			if (matchChar('-')) {
				return token(Type.MINUS_MINUS, "--");
			} else if (matchChar('=')) {
				return token(Type.MINUS_EQ, "-=");
			} else if (matchChar('>')) {
				return token(Type.ARROW, "->");
			} else {
				return token(Type.MINUS, "-");
			}

		case '*':
			if (matchChar('=')) {
				return token(Type.STAR_EQ, "*=");
			} else {
				return token(Type.STAR, "*");
			}

		case '/':
			if (matchChar('=')) {
				return token(Type.SLASH_EQ, "/=");
			} else {
				return token(Type.SLASH, "/");
			}

		case '%':
			if (matchChar('=')) {
				return token(Type.PERCENT_EQ, "%=");
			} else {
				return token(Type.PERCENT, "%");
			}

		case '?':
			if (matchChar('?')) {
				return token(Type.QUESTION_QUESTION, "??");
			} else {
				return token(Type.QUESTION, "?");
			}
			
		case '(':
			return token(Type.LPAREN, "(");
		case ')':
			return token(Type.RPAREN, ")");
		case '{':
			return token(Type.LBRACE, "{");
		case '}':
			return token(Type.RBRACE, "}");
		case '[':
			return token(Type.LBRACKET, "[");
		case ']':
			return token(Type.RBRACKET, "]");
		case ',':
			return token(Type.COMMA, ",");
		case ';':
			return token(Type.SEMICOLON, ";");
		case ':':
			return token(Type.COLON, ":");

		case '.':
			if (check('.') && checkNext('.')) {
				advanceChar();
				advanceChar();
				return token(Type.ELLIPSIS, "...");
			} else {
				return token(Type.DOT, ".");
			}

		case '<':
			if (matchChar('=')) {
				return token(Type.LTE, "<=");
			} else if (matchChar('<')) {
				if (matchChar('=')) {
					return token(Type.AND_EQ, "<<=");
				} else {
					return token(Type.AND, "<<");
				}
			} else {
				return token(Type.LT, "<");
			}

		case '>':
			if (matchChar('=')) {
				return token(Type.GTE, ">=");
			} else if (matchChar('>')) {
				if (matchChar('=')) {
					return token(Type.OR_EQ, ">>=");
				} else {
					return token(Type.OR, ">>");
				}
			} else {
				return token(Type.GT, ">");
			}

		case '!':
			if (matchChar('=')) {
				return token(Type.NOT_EQ, "!=");
			} else {
				return token(Type.NOT, "!");
			}

		case '=':
			if (matchChar('=')) {
				return token(Type.EQ_EQ, "==");
			} else {
				return token(Type.EQ, "=");
			}

		case '&':
			if (matchChar('&')) {
				return token(Type.AND_AND, "&&");
			} else if (matchChar('=')) {
				return token(Type.AND_EQ, "&=");
			} else {
				return token(Type.AND, "&");
			}

		case '|':
			if (matchChar('|')) {
				return token(Type.OR_OR, "||");
			} else if (matchChar('=')) {
				return token(Type.OR_EQ, "|=");
			} else {
				return token(Type.OR, "|");
			}

		case '"':
			return multilineString();

		case '\'':
			return singlelineString();

		default:
			if (isDigit(c)) {
				return number();
			} else if (isAlpha(c)) {
				return identifier();
			} else {
				throw new LexingException("Caractere inesperado: '" + c + "'", getCurrentPosition());
			}
		}
	}

	private Token identifier() {
		while (isAlphaNumeric(peekChar())) advanceChar();

		String text = source.substring(start, current);
		Pair<Type, String> keyword = keywords.get(text);

		if (keyword != null) {
			Type type = keyword.getFirst();
			String lexeme = keyword.getSecond();

			if (type == Type.BOOLEAN) {
				return token(type, lexeme, Boolean.parseBoolean(text));
			} else if (type == Type.NULL) {
				return token(type, lexeme, null);
			} else {
				return token(type, lexeme);
			}
		} else {
			return token(Type.IDENTIFIER, text);
		}
	}

	private Token number() {
		while (isDigit(peekChar())) advanceChar();

		if (peekChar() == '.' && isDigit(peekNextChar())) {
			advanceChar();
			while (isDigit(peekChar())) advanceChar();
		}

		try {
			double value = Double.parseDouble(source.substring(start, current));
			return token(Type.NUMBER, String.valueOf(value), value);
		} catch (NumberFormatException e) {
			throw new LexingException("Número inválido: " + source.substring(start, current), getCurrentPosition(), e);
		}
	}

	private Token singlelineString() {
		while (peekChar() != '\'' && !isAtEndChar()) {
			if (peekChar() == '\n') {
				throw new LexingException("String com aspas simples não pode conter quebra de linha", getCurrentPosition());
			}
			advanceChar();
		}

		if (isAtEndChar()) {
			throw new LexingException("String com aspas simples não terminada", getCurrentPosition());
		}

		advanceChar();

		String value = source.substring(start + 1, current - 1);
		return token(Type.STRING, "'" + value + "'", value);
	}

	private Token multilineString() {
		while (peekChar() != '"' && !isAtEndChar()) {
			if (peekChar() == '\n') {
				line++;
				lineStart = current + 1;
			}
			advanceChar();
		}

		if (isAtEndChar()) {
			throw new LexingException("String com aspas duplas não terminada", getCurrentPosition());
		}

		advanceChar();

		String value = source.substring(start + 1, current - 1);
		return token(Type.STRING, "\"" + value + "\"", value);
	}

	private boolean check(char expected) {
		if (isAtEndChar()) return false;
		return source.charAt(current) == expected;
	}

	private boolean checkNext(char expected) {
		if (current + 1 >= source.length()) return false;
		return source.charAt(current + 1) == expected;
	}

	private boolean matchChar(char expected) {
		if (isAtEndChar()) return false;
		if (source.charAt(current) != expected) return false;
		current++;
		return true;
	}

	private char peekChar() {
		if (isAtEndChar()) return '\0';
		return source.charAt(current);
	}

	private char peekNextChar() {
		if (current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
			   (c >= 'A' && c <= 'Z') ||
			   c == '_';
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isAtEndChar() {
		return current >= source.length();
	}

	private char advanceChar() {
		return source.charAt(current++);
	}

	private Token token(Type type) {
		return token(type, null, null);
	}

	private Token token(Type type, String lexeme) {
		return token(type, lexeme, null);
	}

	private Token token(Type type, String lexeme, Object literal) {
		String text = lexeme != null ? lexeme : source.substring(start, current);
		int column = start - lineStart + 1;
		return Token.of(type, text, literal, line, column, start, current);
	}
}