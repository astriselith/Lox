package com.lox.value;

public class ContinueValue extends Value {
    public static final ContinueValue INSTANCE = new ContinueValue();
    
    private ContinueValue() {}
    
    @Override
    public boolean isContinue() { return true; }
    
    @Override
    public ContinueValue asContinue() { return this; }
    
    @Override
    public String type() { return "continue"; }
    
    @Override
    public boolean truthy() { return false; }
    
    @Override
    public String toString() { return "<continue>"; }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}