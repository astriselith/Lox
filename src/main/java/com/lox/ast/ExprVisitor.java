package com.lox.ast;

public interface ExprVisitor<T> {
    T visitLiteralExpr(LiteralExpr expr);
    T visitDecorExpr(DecorExpr expr);
    T visitVarExpr(VarExpr expr);
    T visitAssignExpr(AssignExpr expr);
    T visitBinaryExpr(BinaryExpr expr);
    T visitUnaryExpr(UnaryExpr expr);
    T visitLogicalExpr(LogicalExpr expr);
    T visitTernaryExpr(TernaryExpr expr);
    T visitCallExpr(CallExpr expr);
    T visitGetExpr(GetExpr expr);
    T visitSetExpr(SetExpr expr);
    T visitIndexGetExpr(IndexGetExpr expr);
    T visitIndexSetExpr(IndexSetExpr expr);
    T visitArrayExpr(ArrayExpr expr);
    T visitBlockExpr(BlockExpr expr);
    T visitVarDeclExpr(VarDeclExpr expr);
    T visitFunDeclExpr(FunDeclExpr expr);
    T visitReturnExpr(ReturnExpr expr);
    T visitThrowExpr(ThrowExpr expr);
    T visitIfExpr(IfExpr expr);
    T visitWhileExpr(WhileExpr expr);
    T visitBreakExpr(BreakExpr expr);
    T visitContinueExpr(ContinueExpr expr);
    T visitParenthesisExpr(ParenthesisExpr expr);
}