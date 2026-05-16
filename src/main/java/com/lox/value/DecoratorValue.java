package com.lox.value;

import java.util.*;

public class DecoratorValue extends TableValue {
    private DecoratorValue(TableValue parent) {
        super(parent);
    }
    
    public static DecoratorValue of(TableValue table) {
        DecoratorValue decorator = new DecoratorValue(table.getParent());
        for (Map.Entry<String, Value> entry : table.asTable().values.entrySet()) {
            decorator.declare(entry.getKey(), entry.getValue());
        }
        return decorator;
    }
    
    public Value init(Value target) {
        if (has("init")) {
            Value initFunc = get("init");
            if (initFunc.isFunction()) {
                List<Value> args = new ArrayList<>();
                args.add(target);
                return initFunc.asFunction().call(this, args);
            }
        }
        return target;
    }
    
    public Value write(Value current, Value newValue) {
        if (has("write")) {
            Value writeFunc = get("write");
            if (writeFunc.isFunction()) {
                List<Value> args = new ArrayList<>();
                args.add(current);
                args.add(newValue);
                return writeFunc.asFunction().call(this, args);
            }
        }
        return newValue;
    }
    
    @Override
    public String type() {
        return "decorator";
    }
    
    @Override
    public boolean isDecorator() { return true; }
    
    @Override
    public DecoratorValue asDecorator() { return this; }
    
    @Override
    public String toString() {
        return "Decorator(" + super.toString() + ")";
    }
}