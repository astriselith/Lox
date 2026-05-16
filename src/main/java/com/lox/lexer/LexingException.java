package com.lox.lexer;

import com.lox.util.Position;

public class LexingException extends RuntimeException {
    private final Position position;
    
    public LexingException(String message, Position position) {
        super(formatMessage(message, position));
        this.position = position;
    }
    
    public LexingException(String message, Position position, Throwable cause) {
        super(formatMessage(message, position), cause);
        this.position = position;
    }
    
    public LexingException(String message) {
        super(message);
        this.position = null;
    }
    
    private static String formatMessage(String message, Position position) {
        if (position == null) return message;
        return String.format("%s [%s]", message, position);
    }
    
    public Position getPosition() {
        return position;
    }
    
    @Override
    public String toString() {
        return getMessage();
    }
}