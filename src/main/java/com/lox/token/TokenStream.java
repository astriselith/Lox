package com.lox.token;

import java.util.ArrayList;
import java.util.List;

public abstract class TokenStream {
 protected final List<Token> cache = new ArrayList<>();
 protected int position = 0;
 protected int fetchPosition = -1;

 private int forwardWindow = 5;
 private int backwardWindow = 5;
 private int maxBacktrackCount = 2;

 private int backtrackLevel = 0;
 private int savedPosition = 0;
 private boolean isBacktracking = false;

 private int eofPosition = -1;

 protected TokenStream() {}

 protected TokenStream(int forwardWindow, int backwardWindow, int maxBacktrackCount) {
		this.forwardWindow = forwardWindow;
		this.backwardWindow = backwardWindow;
		this.maxBacktrackCount = maxBacktrackCount;
 }

 protected abstract Token fetchNextToken();

 private void maintainWindow() {
		int needed = position + forwardWindow;
		while (fetchPosition < needed) {
			if (eofPosition != -1)
				break;
			Token next = fetchNextToken();
			if (next == null) {
				throw new IllegalStateException("Lexer returned null token");
			}
			cache.add(next);
			fetchPosition++;
			if (next.type == Type.EOF) {
				eofPosition = fetchPosition;
				break;
			}
		}

		int minKeep = position - backwardWindow;
		if (minKeep > 0) {
			for (int i = 0; i < minKeep; i++) {
				if (cache.get(i) != null) {
					cache.set(i, null);
				}
			}
		}
 }

 public Token peek() {
		maintainWindow();
		if (position >= cache.size()) {
			return null;
		}
		return cache.get(position);
 }

 public Token peekNext() {
		maintainWindow();
		if (position + 1 >= cache.size()) {
			return null;
		}
		return cache.get(position + 1);
 }

 public Token peekNextNext() {
		maintainWindow();
		if (position + 2 >= cache.size()) {
			return null;
		}
		return cache.get(position + 2);
 }

 public Token peekBack() {
		if (position == 0) {
			throw new IllegalStateException("No previous token available");
		}
		maintainWindow();
		return cache.get(position - 1);
 }

 public Token peekBackBack() {
		if (position < 2) {
			throw new IllegalStateException("No token two positions back");
		}
		maintainWindow();
		return cache.get(position - 2);
 }

 public Token advance() {
		Token current = peek();
		if (current != null && current.type != Type.EOF) {
			position++;
		}
		return current;
 }

 public boolean check(Type type) {
		Token t = peek();
		return t != null && t.type == type;
 }

 public boolean checkNext(Type type) {
		Token t = peekNext();
		return t != null && t.type == type;
 }

 public boolean checkNextNext(Type type) {
		Token t = peekNextNext();
		return t != null && t.type == type;
 }

 public boolean checkBack(Type type) {
		if (position == 0)
			return false;
		Token t = peekBack();
		return t != null && t.type == type;
 }

 public boolean checkBackBack(Type type) {
		if (position < 2)
			return false;
		Token t = peekBackBack();
		return t != null && t.type == type;
 }

 public boolean checkAny(Type... types) {
		Token t = peek();
		if (t == null)
			return false;
		for (Type type : types) {
			if (t.type == type)
				return true;
		}
		return false;
 }

 public boolean checkNextAny(Type... types) {
		Token t = peekNext();
		if (t == null)
			return false;
		for (Type type : types) {
			if (t.type == type)
				return true;
		}
		return false;
 }

 public boolean checkNextNextAny(Type... types) {
		Token t = peekNextNext();
		if (t == null)
			return false;
		for (Type type : types) {
			if (t.type == type)
				return true;
		}
		return false;
 }

 public boolean checkBackAny(Type... types) {
		if (position == 0)
			return false;
		Token t = peekBack();
		if (t == null)
			return false;
		for (Type type : types) {
			if (t.type == type)
				return true;
		}
		return false;
 }

 public boolean checkBackBackAny(Type... types) {
		if (position < 2)
			return false;
		Token t = peekBackBack();
		if (t == null)
			return false;
		for (Type type : types) {
			if (t.type == type)
				return true;
		}
		return false;
 }

 public boolean match(Type t) {
		if (check(t)) {
			Token tk = advance();
			return true;
		}
		return false;
 }

 public boolean matchAny(Type... types) {
		for (Type type : types) {
			if (check(type)) {
				Token tk = advance();
				return true;
			}
		}
		return false;
 }

 public boolean isAtEnd() {
		Token t = peek();
		return t == null || t.type == Type.EOF;
 }

 public void beginBacktrack() {
		if (backtrackLevel >= maxBacktrackCount) {
			throw new IllegalStateException("Maximum backtrack count exceeded: " + maxBacktrackCount);
		}

		if (!isBacktracking) {
			savedPosition = position;
			isBacktracking = true;
		}
		backtrackLevel++;
 }

 public void commitBacktrack() {
		if (backtrackLevel > 0) {
			backtrackLevel--;
			if (backtrackLevel == 0) {
				isBacktracking = false;
				savedPosition = position;
			}
		}
 }

 public void rollbackBacktrack() {
		if (backtrackLevel > 0) {
			position = savedPosition;
			backtrackLevel--;
			if (backtrackLevel == 0) {
				isBacktracking = false;
			}
		}
 }

 public int mark() {
		return position;
 }

 public void reset(int mark) {
		if (mark < 0 || mark > position) {
			throw new IllegalArgumentException("Invalid mark position: " + mark);
		}
		position = mark;

		if (isBacktracking && mark < savedPosition) {
			savedPosition = mark;
		}
 }

 public int getPosition() {
		return position;
 }

 public int getFetchPosition() {
		return fetchPosition;
 }

 public boolean isBacktracking() {
		return isBacktracking;
 }

 public Token previous() {
		return peekBack();
 }

 public void release() {
		for (int i = 0; i < cache.size(); i++) {
			cache.set(i, null);
		}
		cache.clear();
		Token.clearCache();
		position = 0;
		fetchPosition = -1;
 }
}