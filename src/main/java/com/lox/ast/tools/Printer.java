package com.lox.ast.tools;

import com.lox.ast.*;
import java.util.*;

public class Printer implements ExprVisitor<String> {
    private int indent = 0;
    
    private String indentString() {
        return "  ".repeat(indent);
    }
    
    public String print(Expr expr) {
        return expr.accept(this);
    }
    
    private void printList(List<Expr> expressions, StringBuilder sb, boolean needBraces) {
        if (expressions == null || expressions.isEmpty()) {
            if (needBraces) {
                sb.append("{}");
            }
            return;
        }
        
        if (expressions.size() == 1 && !needBraces) {
            sb.append(expressions.get(0).accept(this));
        } else {
            sb.append("{");
            if (expressions.size() > 1 || needBraces) {
                indent++;
                for (Expr e : expressions) {
                    sb.append("\n").append(indentString());
                    sb.append(e.accept(this));
                    if (!(e instanceof ObjectExpr) && !(e instanceof FunDeclExpr)) {
                        sb.append(";");
                    }
                }
                indent--;
                sb.append("\n").append(indentString()).append("}");
            } else {
                sb.append(" ").append(expressions.get(0).accept(this)).append(" }");
            }
        }
    }
    
    @Override
    public String visitLiteralExpr(LiteralExpr expr) {
        if (expr.value == null) return "null";
        if (expr.value instanceof String) return "\"" + expr.value + "\"";
        return expr.value.toString();
    }
    
    @Override
    public String visitVarExpr(VarExpr expr) {
        return expr.name;
    }
    
    @Override
    public String visitAssignExpr(AssignExpr expr) {
        return expr.name + " = " + expr.value.accept(this);
    }
    
    @Override
    public String visitVarDeclExpr(VarDeclExpr expr) {
        StringBuilder sb = new StringBuilder();
        for (Expr d : expr.decorators) {
            sb.append(d.accept(this)).append(" ");
        }
        sb.append("var ").append(expr.name);
        if (expr.value != null) {
            sb.append(" = ").append(expr.value.accept(this));
        }
        return sb.toString();
    }
    
    @Override
    public String visitFunDeclExpr(FunDeclExpr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("fun ");
        if (!expr.name.isEmpty()) {
            sb.append(expr.name);
        }
        sb.append("(");
        if (expr.parameters != null) {
            for (int i = 0; i < expr.parameters.size(); i++) {
                if (i > 0) sb.append(", ");
                if (expr.hasVarargs && i == expr.parameters.size() - 1) {
                    sb.append("...");
                }
                sb.append(expr.parameters.get(i));
            }
        }
        sb.append(") ");
        
        if (expr.getBody().size() == 1 && !(expr.getBody().get(0) instanceof ObjectExpr)) {
            sb.append("-> ").append(expr.getBody().get(0).accept(this));
        } else {
            sb.append("{");
            indent++;
            for (Expr e : expr.getBody()) {
                sb.append("\n").append(indentString()).append(e.accept(this));
                if (!(e instanceof ObjectExpr) && !(e instanceof FunDeclExpr)) {
                    sb.append(";");
                }
            }
            indent--;
            sb.append("\n").append(indentString()).append("}");
        }
        
        return sb.toString();
    }
    
    @Override
    public String visitDecorExpr(DecorExpr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("@").append(expr.name);
        if (expr.arguments != null && !expr.arguments.isEmpty()) {
            sb.append("(");
            for (int i = 0; i < expr.arguments.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(expr.arguments.get(i).accept(this));
            }
            sb.append(")");
        }
        return sb.toString();
    }
    
    @Override
    public String visitObjectExpr(ObjectExpr expr) {
        StringBuilder sb = new StringBuilder();
        printList(expr.getExpressions(), sb, true);
        return sb.toString();
    }
    
    @Override
    public String visitIfExpr(IfExpr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("if (").append(expr.condition.accept(this)).append(") ");
        
        if (expr.thenBranch.size() == 1 && !(expr.thenBranch.get(0) instanceof ObjectExpr)) {
            sb.append(expr.thenBranch.get(0).accept(this));
        } else {
            sb.append("{");
            indent++;
            for (Expr e : expr.thenBranch) {
                sb.append("\n").append(indentString()).append(e.accept(this));
                if (!(e instanceof ObjectExpr) && !(e instanceof FunDeclExpr)) {
                    sb.append(";");
                }
            }
            indent--;
            sb.append("\n").append(indentString()).append("}");
        }
        
        if (expr.elseBranch != null && !expr.elseBranch.isEmpty()) {
            sb.append(" else ");
            if (expr.elseBranch.size() == 1 && !(expr.elseBranch.get(0) instanceof ObjectExpr)) {
                sb.append(expr.elseBranch.get(0).accept(this));
            } else {
                sb.append("{");
                indent++;
                for (Expr e : expr.elseBranch) {
                    sb.append("\n").append(indentString()).append(e.accept(this));
                    if (!(e instanceof ObjectExpr) && !(e instanceof FunDeclExpr)) {
                        sb.append(";");
                    }
                }
                indent--;
                sb.append("\n").append(indentString()).append("}");
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String visitWhileExpr(WhileExpr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("while (").append(expr.condition.accept(this)).append(") ");
        
        if (expr.body.size() == 1 && !(expr.body.get(0) instanceof ObjectExpr)) {
            sb.append(expr.body.get(0).accept(this));
        } else {
            sb.append("{");
            indent++;
            for (Expr e : expr.body) {
                sb.append("\n").append(indentString()).append(e.accept(this));
                if (!(e instanceof ObjectExpr) && !(e instanceof FunDeclExpr)) {
                    sb.append(";");
                }
            }
            indent--;
            sb.append("\n").append(indentString()).append("}");
        }
        
        return sb.toString();
    }
    
    @Override
    public String visitBreakExpr(BreakExpr expr) {
        return "break";
    }
    
    @Override
    public String visitContinueExpr(ContinueExpr expr) {
        return "continue";
    }
    
    @Override
    public String visitBinaryExpr(BinaryExpr expr) {
        return expr.left.accept(this) + " " + expr.operator + " " + expr.right.accept(this);
    }
    
    @Override
    public String visitUnaryExpr(UnaryExpr expr) {
        if (expr.isPrefix()) {
            return expr.operator + expr.operand.accept(this);
        } else if (expr.isPostfix()) {
            return expr.operand.accept(this) + expr.operator;
        }
        return expr.operand.accept(this);
    }
    
    @Override
    public String visitReturnExpr(ReturnExpr expr) {
        if (expr.value != null) {
            return "return " + expr.value.accept(this);
        }
        return "return";
    }
    
    @Override
    public String visitThrowExpr(ThrowExpr expr) {
        if (expr.value != null) {
            return "throw " + expr.value.accept(this);
        }
        return "throw";
    }
    
    @Override
    public String visitLogicalExpr(LogicalExpr expr) {
        return expr.left.accept(this) + " " + expr.operator + " " + expr.right.accept(this);
    }
    
    @Override
    public String visitTernaryExpr(TernaryExpr expr) {
        return expr.condition.accept(this) + " ? " + 
               expr.thenExpr.accept(this) + " : " + 
               expr.elseExpr.accept(this);
    }
    
    @Override
    public String visitCallExpr(CallExpr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append(expr.callee.accept(this)).append("(");
        
        for (int i = 0; i < expr.arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(expr.arguments.get(i).accept(this));
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public String visitGetExpr(GetExpr expr) {
        return expr.object.accept(this) + "." + expr.name;
    }
    
    @Override
    public String visitSetExpr(SetExpr expr) {
        return expr.object.accept(this) + "." + expr.name + " = " + expr.value.accept(this);
    }
    
    @Override
    public String visitIndexGetExpr(IndexGetExpr expr) {
        return expr.array.accept(this) + "[" + expr.index.accept(this) + "]";
    }
    
    @Override
    public String visitIndexSetExpr(IndexSetExpr expr) {
        return expr.array.accept(this) + "[" + expr.index.accept(this) + "] = " + 
               expr.value.accept(this);
    }
    
    @Override
    public String visitArrayExpr(ArrayExpr expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        for (int i = 0; i < expr.elements.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(expr.elements.get(i).accept(this));
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    public String visitParenthesisExpr(ParenthesisExpr expr) {
        return "(" + expr.expression.accept(this) + ")";
    }
}