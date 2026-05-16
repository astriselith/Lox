package com.lox.value;

import com.lox.ast.BlockExpr;

public class BlockValue extends TableValue {
    private final BlockExpr block;
    private final Callable executor;
    private final TableValue closure;
    
    public BlockValue(BlockExpr block, Callable executor, TableValue closure) {
        this.block = block;
        this.executor = executor;
        this.closure = closure;
    }
    
    public BlockExpr getBlock() {
        return block;
    }
    
    @Override
    public boolean isBlock() { return true; }
    
    @Override
    public BlockValue asBlock() { return this; }
    
    @Override
    public String type() { return "block"; }
    
    @Override
    public boolean truthy() { return true; }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}