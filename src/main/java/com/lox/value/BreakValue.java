package com.lox.value;

public class BreakValue extends Value {
    public static final BreakValue INSTANCE = new BreakValue();
    
    private BreakValue() {}
    
    @Override
    public boolean isBreak() { return true; }
    
    @Override
    public BreakValue asBreak() { return this; }
    
    @Override
    public String type() { return "break"; }
    
    @Override
    public boolean truthy() { return false; }
    
    @Override
    public String toString() { return "<break>"; }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}