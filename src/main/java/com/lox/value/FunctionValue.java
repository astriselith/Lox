package com.lox.value;

import com.lox.ast.FunDeclExpr;
import java.util.List;

public class FunctionValue extends Value {
 private final FunDeclExpr declaration;
 private final Callable executor;
 private final TableValue closure;

 public FunctionValue(FunDeclExpr declaration, Callable executor, TableValue closure) {
		this.declaration = declaration;
		this.executor = executor;
		this.closure = closure;
 }

 public FunDeclExpr getDeclaration() {
		return declaration;
 }

 public String getName() {
		return declaration.name != null && !declaration.name.isEmpty() ? declaration.name : "anonymous";
 }

 public Value call(TableValue env, List<Value> arguments) {
		return executor.executeFunction(declaration, env, arguments, closure);
 }

 public int arity() {
		return declaration.parameters != null ? declaration.parameters.size() : 0;
 }

 public boolean hasVarargs() {
		return declaration.hasVarargs;
 }

 public int minArity() {
		return declaration.hasVarargs ? declaration.parameters.size() - 1 : declaration.parameters.size();
 }

 @Override
 public boolean isFunction() {
		return true;
 }

 @Override
 public FunctionValue asFunction() {
		return this;
 }

 @Override
 public String type() {
		return "function";
 }

 @Override
 public boolean truthy() {
		return true;
 }

 @Override
 public String toString() {
		return "<function " + getName() + ">";
 }

 @Override
 public boolean equals(Object obj) {
		return this == obj;
 }
}