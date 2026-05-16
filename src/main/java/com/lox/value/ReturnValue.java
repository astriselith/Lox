package com.lox.value;

import com.lox.ast.ReturnExpr;

public class ReturnValue extends Value {
 private final Value value;
 private final ReturnExpr returnExpr;

 public ReturnValue(Value value, ReturnExpr returnExpr) {
		this.value = value;
		this.returnExpr = returnExpr;
 }

 @Override
 public boolean isReturn() {
		return true;
 }

 @Override
 public ReturnValue asReturn() {
		return this;
 }

 public Value getValue() {
		return value;
 }

 public ReturnExpr getReturnExpr() {
		return returnExpr;
 }

 @Override
 public String type() {
		return "return";
 }

 @Override
 public boolean truthy() {
		return true;
 }

 @Override
 public String toString() {
		return "<return " + value.toString() + ">";
 }

 @Override
 public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReturnValue))
			return false;
		ReturnValue other = (ReturnValue) obj;
		return value.equals(other.value) && returnExpr.equals(other.returnExpr);
 }
}