package com.lox.bytecode;

public class Opcode {
    // ============ Stack Operations ============
    public static final int NOP = 0x00;
    public static final int POP = 0x01;
    public static final int DUP = 0x02;
    public static final int SWAP = 0x03;
    
    // ============ Push Constants ============
    public static final int PUSH_NULL = 0x10;
    public static final int PUSH_TRUE = 0x11;
    public static final int PUSH_FALSE = 0x12;
    public static final int PUSH_NUMBER = 0x13;
    public static final int PUSH_STRING = 0x14;
    public static final int PUSH_INT = 0x15;
    public static final int PUSH_FLOAT = 0x16;
    
    // ============ Local Variables ============
    public static final int LOAD_LOCAL = 0x20;
    public static final int STORE_LOCAL = 0x21;
    public static final int LOAD_LOCAL_0 = 0x22;
    public static final int LOAD_LOCAL_1 = 0x23;
    public static final int LOAD_LOCAL_2 = 0x24;
    public static final int STORE_LOCAL_0 = 0x25;
    public static final int STORE_LOCAL_1 = 0x26;
    public static final int STORE_LOCAL_2 = 0x27;
    
    // ============ Global Variables ============
    public static final int LOAD_GLOBAL = 0x30;
    public static final int STORE_GLOBAL = 0x31;
    public static final int DECLARE_GLOBAL = 0x32;
    
    // ============ Arithmetic Operations ============
    public static final int ADD = 0x40;
    public static final int SUB = 0x41;
    public static final int MUL = 0x42;
    public static final int DIV = 0x43;
    public static final int MOD = 0x44;
    public static final int NEG = 0x45;
    public static final int INC = 0x46;
    public static final int DEC = 0x47;
    
    // ============ Comparison Operations ============
    public static final int EQ = 0x50;
    public static final int NE = 0x51;
    public static final int LT = 0x52;
    public static final int GT = 0x53;
    public static final int LTE = 0x54;
    public static final int GTE = 0x55;
    public static final int CMP = 0x56;
    
    // ============ Logical Operations ============
    public static final int NOT = 0x60;
    public static final int AND = 0x61;
    public static final int OR = 0x62;
    public static final int COALESCE = 0x63;
    
    // ============ Control Flow ============
    public static final int JMP = 0x70;
    public static final int JMP_IF_FALSE = 0x71;
    public static final int JMP_IF_TRUE = 0x72;
    public static final int JMP_IF_NULL = 0x73;
    public static final int JMP_IF_NOT_NULL = 0x74;
    
    // ============ Functions ============
    public static final int CALL = 0x80;
    public static final int CALL_VARARG = 0x81;
    public static final int RETURN = 0x82;
    public static final int MAKE_FUNCTION = 0x83;
    public static final int CLOSE_UPVALUE = 0x84;
    public static final int CAPTURE = 0x85;
    
    // ============ Array Operations ============
    public static final int NEW_ARRAY = 0x90;
    public static final int ARRAY_LENGTH = 0x91;
    public static final int ARRAY_LOAD = 0x92;
    public static final int ARRAY_STORE = 0x93;
    
    // ============ Table/Object Operations ============
    public static final int NEW_TABLE = 0xA0;
    public static final int TABLE_LOAD = 0xA1;
    public static final int TABLE_STORE = 0xA2;
    public static final int TABLE_HAS = 0xA3;
    
    // ============ Type Conversion ============
    public static final int TO_NUMBER = 0xB0;
    public static final int TO_STRING = 0xB1;
    public static final int TO_BOOL = 0xB2;
    public static final int TYPE_OF = 0xB3;
    
    // ============ Exception Handling ============
    public static final int THROW = 0xC0;
    public static final int TRY = 0xC1;
    public static final int CATCH = 0xC2;
    public static final int FINALLY = 0xC3;
    
    // ============ Debug ============
    public static final int HALT = 0xF0;
    public static final int PRINT_STACK = 0xF1;
    public static final int BREAKPOINT = 0xF2;
    
    // ============ Utility Methods ============
    public static String getName(int opcode) {
        switch (opcode) {
            case NOP: return "NOP";
            case POP: return "POP";
            case DUP: return "DUP";
            case SWAP: return "SWAP";
            case PUSH_NULL: return "PUSH_NULL";
            case PUSH_TRUE: return "PUSH_TRUE";
            case PUSH_FALSE: return "PUSH_FALSE";
            case PUSH_NUMBER: return "PUSH_NUMBER";
            case PUSH_STRING: return "PUSH_STRING";
            case PUSH_INT: return "PUSH_INT";
            case PUSH_FLOAT: return "PUSH_FLOAT";
            case LOAD_LOCAL: return "LOAD_LOCAL";
            case STORE_LOCAL: return "STORE_LOCAL";
            case LOAD_LOCAL_0: return "LOAD_LOCAL_0";
            case LOAD_LOCAL_1: return "LOAD_LOCAL_1";
            case LOAD_LOCAL_2: return "LOAD_LOCAL_2";
            case STORE_LOCAL_0: return "STORE_LOCAL_0";
            case STORE_LOCAL_1: return "STORE_LOCAL_1";
            case STORE_LOCAL_2: return "STORE_LOCAL_2";
            case LOAD_GLOBAL: return "LOAD_GLOBAL";
            case STORE_GLOBAL: return "STORE_GLOBAL";
            case DECLARE_GLOBAL: return "DECLARE_GLOBAL";
            case ADD: return "ADD";
            case SUB: return "SUB";
            case MUL: return "MUL";
            case DIV: return "DIV";
            case MOD: return "MOD";
            case NEG: return "NEG";
            case INC: return "INC";
            case DEC: return "DEC";
            case EQ: return "EQ";
            case NE: return "NE";
            case LT: return "LT";
            case GT: return "GT";
            case LTE: return "LTE";
            case GTE: return "GTE";
            case CMP: return "CMP";
            case NOT: return "NOT";
            case AND: return "AND";
            case OR: return "OR";
            case COALESCE: return "COALESCE";
            case JMP: return "JMP";
            case JMP_IF_FALSE: return "JMP_IF_FALSE";
            case JMP_IF_TRUE: return "JMP_IF_TRUE";
            case JMP_IF_NULL: return "JMP_IF_NULL";
            case JMP_IF_NOT_NULL: return "JMP_IF_NOT_NULL";
            case CALL: return "CALL";
            case CALL_VARARG: return "CALL_VARARG";
            case RETURN: return "RETURN";
            case MAKE_FUNCTION: return "MAKE_FUNCTION";
            case CLOSE_UPVALUE: return "CLOSE_UPVALUE";
            case CAPTURE: return "CAPTURE";
            case NEW_ARRAY: return "NEW_ARRAY";
            case ARRAY_LENGTH: return "ARRAY_LENGTH";
            case ARRAY_LOAD: return "ARRAY_LOAD";
            case ARRAY_STORE: return "ARRAY_STORE";
            case NEW_TABLE: return "NEW_TABLE";
            case TABLE_LOAD: return "TABLE_LOAD";
            case TABLE_STORE: return "TABLE_STORE";
            case TABLE_HAS: return "TABLE_HAS";
            case TO_NUMBER: return "TO_NUMBER";
            case TO_STRING: return "TO_STRING";
            case TO_BOOL: return "TO_BOOL";
            case TYPE_OF: return "TYPE_OF";
            case THROW: return "THROW";
            case TRY: return "TRY";
            case CATCH: return "CATCH";
            case FINALLY: return "FINALLY";
            case HALT: return "HALT";
            case PRINT_STACK: return "PRINT_STACK";
            case BREAKPOINT: return "BREAKPOINT";
            default: return "UNKNOWN_" + Integer.toHexString(opcode);
        }
    }
    
    public static boolean isJump(int opcode) {
        return opcode >= JMP && opcode <= JMP_IF_NOT_NULL;
    }
    
    public static boolean isLocalAccess(int opcode) {
        return (opcode >= LOAD_LOCAL && opcode <= STORE_LOCAL_2);
    }
    
    public static int getArity(int opcode) {
        switch (opcode) {
            case LOAD_LOCAL:
            case STORE_LOCAL:
            case LOAD_GLOBAL:
            case STORE_GLOBAL:
            case DECLARE_GLOBAL:
            case PUSH_NUMBER:
            case PUSH_STRING:
            case PUSH_INT:
            case PUSH_FLOAT:
            case JMP:
            case JMP_IF_FALSE:
            case JMP_IF_TRUE:
            case JMP_IF_NULL:
            case JMP_IF_NOT_NULL:
            case CALL:
            case CALL_VARARG:
                return 1;
            case NOP:
            case POP:
            case DUP:
            case SWAP:
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case NEG:
            case INC:
            case DEC:
            case EQ:
            case NE:
            case LT:
            case GT:
            case LTE:
            case GTE:
            case CMP:
            case NOT:
            case AND:
            case OR:
            case COALESCE:
            case RETURN:
            case HALT:
                return 0;
            default:
                return -1;
        }
    }
}