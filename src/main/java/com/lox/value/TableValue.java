package com.lox.value;

import java.util.*;

public class TableValue extends Value {
    private final Map<String, Value> values = new HashMap<>();
    private final TableValue parent;
    private final TableValue global;
    
    public TableValue() {
        this(null, null);
    }
    
    public TableValue(TableValue parent) {
        this(parent, parent != null ? parent.getGlobal() : null);
    }
    
    public TableValue(TableValue parent, TableValue global) {
        this.parent = parent;
        this.global = global != null ? global : this;
    }
    
    @Override
    public boolean isTable() { return true; }
    
    @Override
    public TableValue asTable() { return this; }
    
    public Value get(String name) {
        if (name.equals("global")) {
            return global;
        }
        
        if (values.containsKey(name)) {
            return values.get(name);
        }
        
        if (parent != null) {
            return parent.get(name);
        }
        
        if (global != this && global.has(name)) {
            return global.get(name);
        }
        
        return NullValue.INSTANCE;
    }
    
    public void declare(String name, Value value) {
        if (name.equals("global")) {
            throw new RuntimeException("Não é permitido redeclarar 'global'");
        }
        
        if (values.containsKey(name)) {
            throw new RuntimeException("Variável já declarada: " + name);
        }
        
        values.put(name, value);
    }
    
    public void undeclare(String name) {
        if (name.equals("global")) {
            throw new RuntimeException("Não é permitido remover 'global'");
        }
        
        if (values.containsKey(name)) {
            values.remove(name);
        }
    }
    
    public void set(String name, Value value) {
        if (name.equals("global")) {
            throw new RuntimeException("Não é permitido modificar 'global'");
        }
        
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
        
        if (parent != null) {
            parent.set(name, value);
            return;
        }
        
        if (global != this && global.has(name)) {
            global.set(name, value);
            return;
        }
        
        throw new RuntimeException("Variável não declarada: " + name);
    }
    
    public boolean has(String name) {
        if (name.equals("global")) {
            return true;
        }
        
        if (values.containsKey(name)) {
            return true;
        }
        
        if (parent != null) {
            return parent.has(name);
        }
        
        if (global != this && global.has(name)) {
            return true;
        }
        
        return false;
    }
    
    public boolean hasLocal(String name) {
        if (name.equals("global")) {
            return true;
        }
        return values.containsKey(name);
    }
    
    public boolean isConst(String name) {
        if (name.equals("global")) {
            return false;
        }
        Value v = get(name);
        return v != null && v.isConst();
    }
    
    public TableValue getParent() {
        return parent;
    }
    
    public TableValue getGlobal() {
        return global;
    }
    
    public TableValue child() {
        return new TableValue(this, global);
    }
    
    @Override
    public String type() {
        return "table";
    }
    
    @Override
    public boolean truthy() {
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.identityHashCode(this));
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Value> entry : values.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(entry.getKey()).append(": ").append(entry.getValue().toString());
        }
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TableValue)) return false;
        TableValue other = (TableValue) obj;
        return values.equals(other.values);
    }
}