package com.lox.value;

import java.util.*;

public class ArrayValue extends TableValue {
    private final List<Value> elements = new ArrayList<>();
    
    public ArrayValue() {
        super();
        init();
    }
    
    public ArrayValue(List<Value> elements) {
        super();
        this.elements.addAll(elements);
        init();
    }
    
    @Override
    public boolean isArray() { return true; }
    
    @Override
    public ArrayValue asArray() { return this; }
    
    private void init() {
        declare("length", NumberValue.of(elements.size()));
        
        FunctionValue addFunc = new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                add(arguments.get(0));
                return NullValue.INSTANCE;
            }
            
            @Override
            public int arity() { return 1; }
            @Override
            public boolean hasVarargs() { return false; }
            @Override
            public int minArity() { return 1; }
            @Override
            public String getName() { return "add"; }
        };
        declare("add", addFunc);
        
        FunctionValue removeFunc = new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                if (elements.isEmpty()) return NullValue.INSTANCE;
                return remove();
            }
            
            @Override
            public int arity() { return 0; }
            @Override
            public boolean hasVarargs() { return false; }
            @Override
            public int minArity() { return 0; }
            @Override
            public String getName() { return "remove"; }
        };
        declare("remove", removeFunc);
        
        FunctionValue filterFunc = new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                Value callback = arguments.get(0);
                if (!callback.isFunction()) {
                    return new ThrowValue(new StringValue("filter requer uma função"), null);
                }
                FunctionValue func = callback.asFunction();
                ArrayValue result = new ArrayValue();
                for (int i = 0; i < elements.size(); i++) {
                    Value shouldKeep = func.call(env, List.of(elements.get(i), NumberValue.of(i)));
                    if (shouldKeep.isThrow() || shouldKeep.isReturn()) {
                        return shouldKeep;
                    }
                    if (shouldKeep.truthy()) {
                        result.add(elements.get(i));
                    }
                }
                return result;
            }
            
            @Override
            public int arity() { return 1; }
            @Override
            public boolean hasVarargs() { return false; }
            @Override
            public int minArity() { return 1; }
            @Override
            public String getName() { return "filter"; }
        };
        declare("filter", filterFunc);
        
        FunctionValue joinFunc = new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                String separator = arguments.get(0).toString();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < elements.size(); i++) {
                    if (i > 0) sb.append(separator);
                    sb.append(elements.get(i).toString());
                }
                return new StringValue(sb.toString());
            }
            
            @Override
            public int arity() { return 1; }
            @Override
            public boolean hasVarargs() { return false; }
            @Override
            public int minArity() { return 1; }
            @Override
            public String getName() { return "join"; }
        };
        declare("join", joinFunc);
        
        FunctionValue containsFunc = new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                Value search = arguments.get(0);
                for (Value element : elements) {
                    if (element.equals(search)) {
                        return BooleanValue.TRUE;
                    }
                }
                return BooleanValue.FALSE;
            }
            
            @Override
            public int arity() { return 1; }
            @Override
            public boolean hasVarargs() { return false; }
            @Override
            public int minArity() { return 1; }
            @Override
            public String getName() { return "contains"; }
        };
        declare("contains", containsFunc);
    }
    
    public void add(Value value) {
        elements.add(value);
        set("length", NumberValue.of(elements.size()));
    }
    
    public Value remove() {
        if (elements.isEmpty()) return NullValue.INSTANCE;
        Value removed = elements.remove(elements.size() - 1);
        set("length", NumberValue.of(elements.size()));
        return removed;
    }
    
    public int length() {
        return elements.size();
    }
    
    public List<Value> getElements() {
        return new ArrayList<>(elements);
    }
    
    public Value get(int index) {
        if (index < 0 || index >= elements.size()) {
            return new ThrowValue(new StringValue("Índice fora dos limites: " + index), null);
        }
        return elements.get(index);
    }
    
    public void set(int index, Value value) {
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeException("Índice fora dos limites: " + index);
        }
        elements.set(index, value);
    }
    
    @Override
    public String type() {
        return "array";
    }
    
    @Override
    public boolean truthy() {
        return !elements.isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(elements.get(i).toString());
        }
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArrayValue)) return false;
        ArrayValue other = (ArrayValue) obj;
        return elements.equals(other.elements);
    }
}