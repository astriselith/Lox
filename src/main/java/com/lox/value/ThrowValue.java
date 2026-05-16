package com.lox.value;

import com.lox.ast.ThrowExpr;

public class ThrowValue extends Value {
 private final Value value;
 private final ThrowExpr throwExpr;

 public ThrowValue(Value value, ThrowExpr throwExpr) {
		this.value = value;
		this.throwExpr = throwExpr;
 }

 @Override
 public boolean isThrow() {
		return true;
 }

 @Override
 public ThrowValue asThrow() {
		return this;
 }

 public Value getValue() {
		return value;
 }

 public ThrowExpr getThrowExpr() {
		return throwExpr;
 }

 @Override
 public String type() {
		return "throw";
 }

 @Override
 public boolean truthy() {
		return false;
 }

 @Override
 public String toString() {
		return "<throw " + value.toString() + ">";
 }

 @Override
 public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ThrowValue))
			return false;
		ThrowValue other = (ThrowValue) obj;
		return value.equals(other.value) && throwExpr.equals(other.throwExpr);
 }
}