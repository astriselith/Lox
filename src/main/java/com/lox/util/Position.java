package com.lox.util;

public class Position {
    private final int line;
    private final int column;
    private final int start;
    private final int end;
    
    public static final Position ZERO = new Position(0, 0, 0, 0);
    
    public Position(int line, int column, int start, int end) {
        this.line = line;
        this.column = column;
        this.start = start;
        this.end = end;
    }
    
    public Position(int line, int start, int end) {
        this(line, start + 1, start, end);
    }
    
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
    
    public static Position between(Position start, Position end) {
        return new Position(start.line, start.column, start.start, end.end);
    }
    
    @Override
    public String toString() {
        return String.format("line %d, col %d, pos %d-%d", line, column, start, end);
    }
}