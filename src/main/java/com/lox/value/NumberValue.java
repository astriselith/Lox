package com.lox.value;

import java.util.HashMap;
import java.util.Map;

public class NumberValue extends Value {
    private static final Map<Double, NumberValue> CACHE = new HashMap<>();
    private static final int CACHE_MIN = -128;
    private static final int CACHE_MAX = 127;
    
    private final double value;
    
    private NumberValue(double value) {
        this.value = value;
    }
    
    public static NumberValue of(double value) {
        if (value == (long) value) {
            long longValue = (long) value;
            if (longValue >= CACHE_MIN && longValue <= CACHE_MAX) {
                return CACHE.computeIfAbsent(value, NumberValue::new);
            }
        }
        return new NumberValue(value);
    }
    
    public double getValue() {
        return value;
    }
    
    @Override
    public boolean isNumber() { return true; }
    
    @Override
    public double asNumber() { return value; }
    
    @Override
    public String type() {
        return "number";
    }
    
    @Override
    public boolean truthy() {
        return value != 0;
    }
    
    @Override
    public String toString() {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NumberValue)) return false;
        return Double.compare(value, ((NumberValue) obj).value) == 0;
    }
    
    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
}