package com.lox.value;

import java.util.List;

public class StringValue extends TableValue {
    private final String value;
    
    public StringValue(String value) {
        super();
        this.value = value;
        init();
    }
    
    private void init() {
        declare("length", NumberValue.of(value.length()));
        
        declare("repeat", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                if (!arguments.get(0).isNumber()) {
                    return new ThrowValue(new StringValue("repeat requer um número"), null);
                }
                int count = (int) arguments.get(0).asNumber();
                if (count < 0) {
                    return new ThrowValue(new StringValue("repeat não aceita número negativo"), null);
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    sb.append(value);
                }
                return new StringValue(sb.toString());
            }
            
            @Override
            public int arity() { return 1; }
        });
        
        declare("toUpperCase", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                return new StringValue(value.toUpperCase());
            }
            
            @Override
            public int arity() { return 0; }
        });
        
        declare("toLowerCase", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                return new StringValue(value.toLowerCase());
            }
            
            @Override
            public int arity() { return 0; }
        });
        
        declare("substring", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                if (!arguments.get(0).isNumber()) {
                    return new ThrowValue(new StringValue("substring requer índice inicial"), null);
                }
                if (!arguments.get(1).isNumber()) {
                    return new ThrowValue(new StringValue("substring requer índice final"), null);
                }
                
                int start = (int) arguments.get(0).asNumber();
                int end = (int) arguments.get(1).asNumber();
                
                if (start < 0 || start > value.length() || end < start || end > value.length()) {
                    return new ThrowValue(new StringValue("Índices inválidos para substring"), null);
                }
                
                return new StringValue(value.substring(start, end));
            }
            
            @Override
            public int arity() { return 2; }
        });
        
        declare("split", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                String delimiter = arguments.get(0).toString();
                String[] parts = value.split(delimiter);
                ArrayValue result = new ArrayValue();
                for (String part : parts) {
                    result.add(new StringValue(part));
                }
                return result;
            }
            
            @Override
            public int arity() { return 1; }
        });
        
        declare("charAt", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                if (!arguments.get(0).isNumber()) {
                    return new ThrowValue(new StringValue("charAt requer um índice"), null);
                }
                int index = (int) arguments.get(0).asNumber();
                if (index < 0 || index >= value.length()) {
                    return new ThrowValue(new StringValue("Índice fora dos limites"), null);
                }
                return new StringValue(String.valueOf(value.charAt(index)));
            }
            
            @Override
            public int arity() { return 1; }
        });
        
        declare("indexOf", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                String search = arguments.get(0).toString();
                return NumberValue.of(value.indexOf(search));
            }
            
            @Override
            public int arity() { return 1; }
        });
        
        declare("startsWith", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                String prefix = arguments.get(0).toString();
                return BooleanValue.of(value.startsWith(prefix));
            }
            
            @Override
            public int arity() { return 1; }
        });
        
        declare("endsWith", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                String suffix = arguments.get(0).toString();
                return BooleanValue.of(value.endsWith(suffix));
            }
            
            @Override
            public int arity() { return 1; }
        });
        
        declare("trim", new FunctionValue(null, null, null) {
            @Override
            public Value call(TableValue env, List<Value> arguments) {
                return new StringValue(value.trim());
            }
            
            @Override
            public int arity() { return 0; }
        });
        
        lock();
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean isString() { return true; }
    
    @Override
    public String asString() { return value; }
    
    @Override
    public String type() {
        return "string";
    }
    
    @Override
    public boolean truthy() {
        return !value.isEmpty();
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StringValue)) return false;
        return value.equals(((StringValue) obj).value);
    }
}