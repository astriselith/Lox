package com.lox.value;

import com.lox.ast.*;
import java.util.List;

public interface Callable {
    
    Value executeFunction(FunDeclExpr function, TableValue env, TableValue closure);
    
    Value executeFunction(FunDeclExpr function, TableValue callEnv, 
                          List<Value> arguments, TableValue closure);
    
    Value execute(List<Expr> expr);
}