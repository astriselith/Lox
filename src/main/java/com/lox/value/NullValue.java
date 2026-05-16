package com.lox.value;

public class NullValue extends Value {
 public static final NullValue INSTANCE = new NullValue();

 private NullValue() {}

 @Override
 public boolean isNull() {
		return true;
 }

 @Override
 public String type() {
		return "null";
 }

 @Override
 public boolean truthy() {
		return false;
 }

 @Override
 public String toString() {
		return "null";
 }

 @Override
 public boolean equals(Object obj) {
		return obj instanceof NullValue;
 }
}