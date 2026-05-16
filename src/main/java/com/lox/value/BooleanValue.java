package com.lox.value;

public class BooleanValue extends Value {
 public static final BooleanValue TRUE = new BooleanValue(true);
 public static final BooleanValue FALSE = new BooleanValue(false);

 private final boolean value;

 private BooleanValue(boolean value) {
		this.value = value;
 }

 public static BooleanValue of(boolean value) {
		return value ? TRUE : FALSE;
 }

 public boolean getValue() {
		return value;
 }

 @Override
 public boolean isBoolean() {
		return true;
 }

 @Override
 public boolean asBoolean() {
		return value;
 }

 @Override
 public String type() {
		return "boolean";
 }

 @Override
 public boolean truthy() {
		return value;
 }

 @Override
 public String toString() {
		return String.valueOf(value);
 }

 @Override
 public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof BooleanValue))
			return false;
		return value == ((BooleanValue) obj).value;
 }
}