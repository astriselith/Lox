package com.lox.value;

public abstract class Value {
 public abstract String type();
 public abstract boolean truthy();

 // Métodos de verificação de tipo
 public boolean isNumber() {
		return false;
 }
 public boolean isString() {
		return false;
 }
 public boolean isBoolean() {
		return false;
 }
 public boolean isNull() {
		return false;
 }
 public boolean isTable() {
		return false;
 }
 public boolean isArray() {
		return false;
 }
 public boolean isFunction() {
		return false;
 }
 public boolean isReturn() {
		return false;
 }
 public boolean isThrow() {
		return false;
 }
 public boolean isBreak() {
		return false;
 }
 public boolean isContinue() {
		return false;
 }
 public boolean isDecorator() {
		return false;
 }

 // Métodos de acesso para tipos específicos
 public double asNumber() {
		throw new RuntimeException("Não é um número");
 }
 public String asString() {
		throw new RuntimeException("Não é uma string");
 }
 public boolean asBoolean() {
		throw new RuntimeException("Não é um booleano");
 }
 public TableValue asTable() {
		throw new RuntimeException("Não é uma tabela");
 }
 public ArrayValue asArray() {
		throw new RuntimeException("Não é um array");
 }
 public FunctionValue asFunction() {
		throw new RuntimeException("Não é uma função");
 }
 public ReturnValue asReturn() {
		throw new RuntimeException("Não é um return");
 }
 public ThrowValue asThrow() {
		throw new RuntimeException("Não é um throw");
 }
 public BreakValue asBreak() {
		throw new RuntimeException("Não é um break");
 }
 public ContinueValue asContinue() {
		throw new RuntimeException("Não é um continue");
 }
 public DecoratorValue asDecorator() {
		throw new RuntimeException("Não é um decorator");
 }

 @Override public abstract String toString();

 @Override public abstract boolean equals(Object obj);
}