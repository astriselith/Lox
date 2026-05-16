package com.lox.bytecode;

public class Instruction {
    public final int opcode;
    public final Object operand;
    public final int line;
    
    public Instruction(int opcode) {
        this(opcode, null, -1);
    }
    
    public Instruction(int opcode, Object operand) {
        this(opcode, operand, -1);
    }
    
    public Instruction(int opcode, Object operand, int line) {
        this.opcode = opcode;
        this.operand = operand;
        this.line = line;
    }
    
    public boolean hasOperand() {
        return operand != null;
    }
    
    public int asInt() {
        return (int) operand;
    }
    
    public double asDouble() {
        return (double) operand;
    }
    
    public String asString() {
        return (String) operand;
    }
    
    @Override
    public String toString() {
        String name = Opcode.getName(opcode);
        if (operand != null) {
            return String.format("%-16s %s", name, operand);
        }
        return name;
    }
}