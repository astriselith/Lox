package com.lox.executor;

import com.lox.ast.*;
import com.lox.value.*;
import com.lox.util.*;
import java.util.*;

public class Executor implements ExprVisitor<Value>, Callable {
	private TableValue current;
	private final TableValue global;

	public Executor() {
		this.global = new TableValue();
		this.current = global;
		defineBuiltins();
	}

	public TableValue getGlobal() {
		return global;
	}

	private void defineBuiltins() {
		// println
		FunctionValue println = new FunctionValue(null, this, global) {
			@Override
			public Value call(TableValue env, List<Value> arguments) {
				System.out.println(arguments.get(0).toString());
				return NullValue.INSTANCE;
			}
			
			@Override
			public int arity() { return 1; }
			@Override
			public boolean hasVarargs() { return false; }
			@Override
			public int minArity() { return 1; }
			@Override
			public String getName() { return "println"; }
		};
		global.declare("println", println);
		
		// print
		FunctionValue print = new FunctionValue(null, this, global) {
			@Override
			public Value call(TableValue env, List<Value> arguments) {
				System.out.print(arguments.get(0).toString());
				return NullValue.INSTANCE;
			}
			
			@Override
			public int arity() { return 1; }
			@Override
			public boolean hasVarargs() { return false; }
			@Override
			public int minArity() { return 1; }
			@Override
			public String getName() { return "print"; }
		};
		global.declare("print", print);
		
		// typeof
		FunctionValue typeof = new FunctionValue(null, this, global) {
			@Override
			public Value call(TableValue env, List<Value> arguments) {
				return new StringValue(arguments.get(0).type());
			}
			
			@Override
			public int arity() { return 1; }
			@Override
			public boolean hasVarargs() { return false; }
			@Override
			public int minArity() { return 1; }
			@Override
			public String getName() { return "typeof"; }
		};
		global.declare("typeof", typeof);
		
		// sqrt
		FunctionValue sqrt = new FunctionValue(null, this, global) {
			@Override
			public Value call(TableValue env, List<Value> arguments) {
				return NumberValue.of(Math.sqrt(arguments.get(0).asNumber()));
			}
			
			@Override
			public int arity() { return 1; }
			@Override
			public boolean hasVarargs() { return false; }
			@Override
			public int minArity() { return 1; }
			@Override
			public String getName() { return "sqrt"; }
		};
		global.declare("sqrt", sqrt);
	}

	private ThrowValue throwError(String message, Position position) {
		return new ThrowValue(new StringValue(message + " em " + position), null);
	}

	private ThrowValue throwError(String message) {
		return new ThrowValue(new StringValue(message), null);
	}

	private TableValue prepareFunctionExecution(
		TableValue closure,
		List<String> params,
		boolean hasVarargs,
		List<Value> arguments) {

		TableValue newTable = closure.child();

		if (params != null && !params.isEmpty()) {
			int paramCount = params.size();
			int argCount = arguments.size();

			if (hasVarargs) {
				int regularParams = paramCount - 1;

				for (int i = 0; i < regularParams; i++) {
					if (i < argCount) {
						newTable.declare(params.get(i), arguments.get(i));
					} else {
						newTable.declare(params.get(i), NullValue.INSTANCE);
					}
				}

				List<Value> varArgsArray = new ArrayList<>();
				for (int i = regularParams; i < argCount; i++) {
					varArgsArray.add(arguments.get(i));
				}
				newTable.declare(params.get(regularParams), new ArrayValue(varArgsArray));

			} else {
				for (int i = 0; i < paramCount; i++) {
					if (i < argCount) {
						newTable.declare(params.get(i), arguments.get(i));
					} else {
						newTable.declare(params.get(i), NullValue.INSTANCE);
					}
				}
			}
		}

		return newTable;
	}

	public Value executeFunction(FunDeclExpr function, TableValue env, TableValue closure) {
		return executeFunction(function, env, new ArrayList<>(), closure);
	}

	public Value executeFunction(FunDeclExpr function, TableValue callEnv,
								 List<Value> arguments, TableValue closure) {
		int minArgs = function.hasVarargs
					  ? function.parameters.size() - 1
					  : function.parameters.size();
		int maxArgs = function.hasVarargs
					  ? Integer.MAX_VALUE
					  : function.parameters.size();
		int argCount = arguments.size();

		if (argCount < minArgs) {
			String funcName = function.name != null ? function.name : "anonymous";
			return throwError("Função " + funcName +
							  " espera no mínimo " + minArgs + " argumentos, recebeu " + argCount);
		}

		if (!function.hasVarargs && argCount != maxArgs) {
			String funcName = function.name != null ? function.name : "anonymous";
			return throwError("Função " + funcName +
							  " espera exatamente " + maxArgs + " argumentos, recebeu " + argCount);
		}

		TableValue funcEnv = prepareFunctionExecution(
								 closure,
								 function.parameters,
								 function.hasVarargs,
								 arguments
							 );

		TableValue previous = this.current;
		this.current = funcEnv;

		Value result = NullValue.INSTANCE;

		for (Expr e : function.getBody()) {
			Value eval = e.accept(this);

			if (eval.isReturn()) {
				ReturnValue ret = eval.asReturn();
				if (ret.getReturnExpr().enclosingFunction == function) {
					this.current = previous;
					return ret.getValue();
				}
				this.current = previous;
				return ret;
			}

			if (eval.isThrow()) {
				this.current = previous;
				return eval;
			}

			result = eval;
		}

		this.current = previous;
		return result;
	}

	private Value execute(List<Expr> statements, TableValue env) {
		TableValue previous = this.current;
		this.current = env.child();

		Value result = NullValue.INSTANCE;

		for (Expr e : statements) {
			Value eval = e.accept(this);

			if (eval.isReturn() || eval.isThrow()) {
				this.current = previous;
				return eval;
			}

			if (eval.isBreak() || eval.isContinue()) {
				this.current = previous;
				return eval;
			}

			result = eval;
		}

		this.current = previous;
		return result;
	}

	public Value execute(List<Expr> statements) {
		return execute(statements, current);
	}

	@Override
	public Value visitLiteralExpr(LiteralExpr expr) {
		Object value = expr.value;
		if (value == null) return NullValue.INSTANCE;
		if (value instanceof Double) return NumberValue.of((Double) value);
		if (value instanceof String) return new StringValue((String) value);
		if (value instanceof Boolean) return BooleanValue.of((Boolean) value);
		return new StringValue(value.toString());
	}

	@Override
	public Value visitVarExpr(VarExpr expr) {
		if (current.has(expr.name)) {
			return current.get(expr.name);
		}
		return throwError("Variável não definida: " + expr.name, expr.getPosition());
	}

	@Override
	public Value visitAssignExpr(AssignExpr expr) {
		Value value = expr.value.accept(this);
		if (value.isReturn() || value.isThrow()) return value;

		if (!current.has(expr.name)) {
			return throwError("Variável não definida: " + expr.name, expr.getPosition());
		}

		try {
			current.set(expr.name, value);
			return value;
		} catch (RuntimeException e) {
			return throwError(e.getMessage(), expr.getPosition());
		}
	}

	@Override
	public Value visitVarDeclExpr(VarDeclExpr expr) {
		Value value = NullValue.INSTANCE;
		if (expr.value != null) {
			Value val = expr.value.accept(this);
			if (val.isReturn() || val.isThrow()) return val;
			value = val;
		}
		
		// Processa os decorators
		List<DecoratorValue> decoratorValues = new ArrayList<>();
		for (Expr decor : expr.decorators) {
			Value decorResult = decor.accept(this);
			if (decorResult.isReturn() || decorResult.isThrow()) return decorResult;
			
			if (!(decorResult instanceof DecoratorValue)) {
				return throwError("Decorator deve retornar um DecoratorValue", expr.getPosition());
			}
			
			decoratorValues.add((DecoratorValue) decorResult);
		}

		try {
			current.declare(expr.name, value, decoratorValues);
			return value;
		} catch (RuntimeException e) {
			return throwError(e.getMessage(), expr.getPosition());
		}
	}

	@Override
	public Value visitFunDeclExpr(FunDeclExpr expr) {
		FunctionValue func = new FunctionValue(expr, this, current);
		try {
			if (!expr.anonymous)
				current.declare(expr.name, func);
			return func;
		} catch (RuntimeException e) {
			return throwError(e.getMessage(), expr.getPosition());
		}
	}

	@Override
	public Value visitDecorExpr(DecorExpr expr) {
		// Obtém a função decorator do escopo atual
		Value decorFunc = current.get(expr.name);
		if (decorFunc.isThrow()) return decorFunc;
		
		if (!decorFunc.isFunction()) {
			return throwError("Decorator '" + expr.name + "' não é uma função", expr.getPosition());
		}
		
		// Avalia os argumentos
		List<Value> args = new ArrayList<>();
		if (expr.arguments != null) {
			for (Expr arg : expr.arguments) {
				Value argVal = arg.accept(this);
				if (argVal.isReturn() || argVal.isThrow()) return argVal;
				args.add(argVal);
			}
		}
		
		// Chama a função decorator, que deve retornar uma tabela
		Value result = decorFunc.asFunction().call(current, args);
		if (result.isReturn() || result.isThrow()) return result;
		
		if (!result.isTable()) {
			return throwError("Decorator '" + expr.name + "' deve retornar uma tabela", expr.getPosition());
		}
		
		// Cria e retorna um DecoratorValue usando of()
		return DecoratorValue.of(result.asTable());
	}

	@Override
	public Value visitObjectExpr(ObjectExpr expr) {
		TableValue previous = this.current;
		TableValue blockEnv = this.current.child();
		this.current = blockEnv;

		for (Expr e : expr.getExpressions()) {
			Value eval = e.accept(this);

			if (eval.isReturn() || eval.isThrow() || eval.isBreak() || eval.isContinue()) {
				this.current = previous;
				return eval;
			}
		}

		this.current = previous;
		return blockEnv;
	}

	@Override
	public Value visitReturnExpr(ReturnExpr expr) {
		if (expr.value != null) {
			Value value = expr.value.accept(this);
			if (value.isThrow()) return value;
			return new ReturnValue(value, expr);
		}
		return new ReturnValue(NullValue.INSTANCE, expr);
	}

	@Override
	public Value visitThrowExpr(ThrowExpr expr) {
		if (expr.value != null) {
			Value value = expr.value.accept(this);
			if (value.isReturn()) return value;
			return new ThrowValue(value, expr);
		}
		return new ThrowValue(NullValue.INSTANCE, expr);
	}

	@Override
	public Value visitIfExpr(IfExpr expr) {
		Value condition = expr.condition.accept(this);
		if (condition.isReturn() || condition.isThrow()) return condition;

		if (condition.truthy()) {
			TableValue previous = this.current;
			TableValue branchEnv = this.current.child();
			this.current = branchEnv;

			for (Expr e : expr.thenBranch) {
				Value eval = e.accept(this);

				if (eval.isReturn() || eval.isThrow() || eval.isBreak() || eval.isContinue()) {
					this.current = previous;
					return eval;
				}
			}

			this.current = previous;
			return NullValue.INSTANCE;
		} else if (expr.elseBranch != null) {
			TableValue previous = this.current;
			TableValue branchEnv = this.current.child();
			this.current = branchEnv;

			for (Expr e : expr.elseBranch) {
				Value eval = e.accept(this);

				if (eval.isReturn() || eval.isThrow() || eval.isBreak() || eval.isContinue()) {
					this.current = previous;
					return eval;
				}
			}

			this.current = previous;
			return NullValue.INSTANCE;
		}

		return NullValue.INSTANCE;
	}

	@Override
	public Value visitWhileExpr(WhileExpr expr) {
		boolean shouldBreak = false;

		while (!shouldBreak) {
			Value condition = expr.condition.accept(this);

			if (condition.isReturn() || condition.isThrow()) return condition;

			if (!condition.truthy()) {
				break;
			}

			TableValue previous = this.current;
			TableValue loopEnv = this.current.child();
			this.current = loopEnv;

			for (Expr e : expr.body) {
				Value eval = e.accept(this);

				if (eval.isReturn() || eval.isThrow()) {
					this.current = previous;
					return eval;
				}

				if (eval.isBreak()) {
					shouldBreak = true;
					break;
				}

				if (eval.isContinue()) {
					break;
				}
			}

			this.current = previous;
		}

		return NullValue.INSTANCE;
	}

	@Override
	public Value visitBreakExpr(BreakExpr expr) {
		return BreakValue.INSTANCE;
	}

	@Override
	public Value visitContinueExpr(ContinueExpr expr) {
		return ContinueValue.INSTANCE;
	}

	@Override
	public Value visitBinaryExpr(BinaryExpr expr) {
		String op = expr.operator;

		if (op.equals("??")) {
			Value left = expr.left.accept(this);
			if (left.isReturn() || left.isThrow()) return left;

			if (!left.isNull()) {
				return left;
			}

			Value right = expr.right.accept(this);
			if (right.isReturn() || right.isThrow()) return right;
			return right;
		}

		Value left = expr.left.accept(this);
		if (left.isReturn() || left.isThrow()) return left;

		Value right = expr.right.accept(this);
		if (right.isReturn() || right.isThrow()) return right;

		try {
			switch (op) {
			case "+":
				if (left.isNumber() && right.isNumber()) {
					return NumberValue.of(left.asNumber() + right.asNumber());
				}
				return new StringValue(left.toString() + right.toString());
			case "-":
				if (!left.isNumber() || !right.isNumber()) {
					return throwError("Operador - requer números", expr.getPosition());
				}
				return NumberValue.of(left.asNumber() - right.asNumber());
			case "*":
				if (!left.isNumber() || !right.isNumber()) {
					return throwError("Operador * requer números", expr.getPosition());
				}
				return NumberValue.of(left.asNumber() * right.asNumber());
			case "/":
				if (!left.isNumber() || !right.isNumber()) {
					return throwError("Operador / requer números", expr.getPosition());
				}
				double r = right.asNumber();
				if (r == 0) return throwError("Divisão por zero", expr.getPosition());
				return NumberValue.of(left.asNumber() / r);
			case "%":
				if (!left.isNumber() || !right.isNumber()) {
					return throwError("Operador % requer números", expr.getPosition());
				}
				return NumberValue.of(left.asNumber() % right.asNumber());
			case "==":
				return BooleanValue.of(left.equals(right));
			case "!=":
				return BooleanValue.of(!left.equals(right));
			case "<":
				if (!left.isNumber() || !right.isNumber()) {
					return throwError("Operador < requer números", expr.getPosition());
				}
				return BooleanValue.of(left.asNumber() < right.asNumber());
			case ">":
				if (!left.isNumber() || !right.isNumber()) {
					return throwError("Operador > requer números", expr.getPosition());
				}
				return BooleanValue.of(left.asNumber() > right.asNumber());
			case "<=":
				if (!left.isNumber() || !right.isNumber()) {
					return throwError("Operador <= requer números", expr.getPosition());
				}
				return BooleanValue.of(left.asNumber() <= right.asNumber());
			case ">=":
				if (!left.isNumber() || !right.isNumber()) {
					return throwError("Operador >= requer números", expr.getPosition());
				}
				return BooleanValue.of(left.asNumber() >= right.asNumber());
			default:
				return throwError("Operador desconhecido: " + op, expr.getPosition());
			}
		} catch (Exception e) {
			return throwError("Operação inválida: " + op, expr.getPosition());
		}
	}

	@Override
	public Value visitUnaryExpr(UnaryExpr expr) {
		Value operand = expr.operand.accept(this);
		if (operand.isReturn() || operand.isThrow()) return operand;

		String op = expr.operator;

		try {
			if (op.equals("++") || op.equals("--")) {
				if (!operand.isNumber()) {
					return throwError("Incremento/decremento requer número", expr.getPosition());
				}

				double val = operand.asNumber();
				double newVal = op.equals("++") ? val + 1 : val - 1;

				Expr target = expr.operand;
				Value newValue = NumberValue.of(newVal);

				try {
					if (target instanceof VarExpr) {
						VarExpr var = (VarExpr) target;
						if (!current.has(var.name)) {
							return throwError("Variável não definida: " + var.name, expr.getPosition());
						}
						current.set(var.name, newValue);
					} else if (target instanceof GetExpr) {
						GetExpr get = (GetExpr) target;
						Value object = get.object.accept(this);
						if (object.isReturn() || object.isThrow()) return object;
						if (object.isTable()) {
							if (!object.asTable().has(get.name)) {
								return throwError("Propriedade não definida: " + get.name, expr.getPosition());
							}
							object.asTable().set(get.name, newValue);
						} else {
							return throwError("Apenas tabelas podem ter campos", expr.getPosition());
						}
					} else if (target instanceof IndexGetExpr) {
						IndexGetExpr idxGet = (IndexGetExpr) target;
						Value array = idxGet.array.accept(this);
						if (array.isReturn() || array.isThrow()) return array;
						Value index = idxGet.index.accept(this);
						if (index.isReturn() || index.isThrow()) return index;
						if (array.isArray() && index.isNumber()) {
							int idx = (int) index.asNumber();
							if (idx < 0 || idx >= array.asArray().length()) {
								return throwError("Índice fora dos limites: " + idx, expr.getPosition());
							}
							array.asArray().set(idx, newValue);
						} else {
							return throwError("Índice inválido", expr.getPosition());
						}
					} else {
						return throwError("Alvo de incremento/decremento inválido", expr.getPosition());
					}
				} catch (RuntimeException e) {
					return throwError(e.getMessage(), expr.getPosition());
				}

				if (expr.isPrefix()) {
					return NumberValue.of(newVal);
				} else {
					return NumberValue.of(val);
				}
			}

			switch (op) {
			case "-":
				if (!operand.isNumber()) {
					return throwError("Operador - requer número", expr.getPosition());
				}
				return NumberValue.of(-operand.asNumber());
			case "!":
				return BooleanValue.of(!operand.truthy());
			default:
				return throwError("Operador unário desconhecido: " + op, expr.getPosition());
			}
		} catch (Exception e) {
			return throwError("Operação inválida: " + op, expr.getPosition());
		}
	}

	@Override
	public Value visitLogicalExpr(LogicalExpr expr) {
		Value left = expr.left.accept(this);
		if (left.isReturn() || left.isThrow()) return left;

		String op = expr.operator;

		if (op.equals("||")) {
			if (left.truthy()) return left;
			return expr.right.accept(this);
		} else {
			if (!left.truthy()) return left;
			return expr.right.accept(this);
		}
	}

	@Override
	public Value visitTernaryExpr(TernaryExpr expr) {
		Value condition = expr.condition.accept(this);
		if (condition.isReturn() || condition.isThrow()) return condition;

		if (condition.truthy()) {
			return expr.thenExpr.accept(this);
		} else {
			return expr.elseExpr.accept(this);
		}
	}

	@Override
	public Value visitCallExpr(CallExpr expr) {
		Value callee = expr.callee.accept(this);
		if (callee.isReturn() || callee.isThrow()) return callee;

		List<Value> arguments = new ArrayList<>();
		for (Expr arg : expr.arguments) {
			Value argVal = arg.accept(this);
			if (argVal.isReturn() || argVal.isThrow()) return argVal;
			arguments.add(argVal);
		}

		if (callee.isFunction()) {
			FunctionValue function = callee.asFunction();

			int minArgs = function.minArity();
			int maxArgs = function.hasVarargs() ? Integer.MAX_VALUE : function.arity();
			int argCount = arguments.size();

			if (argCount < minArgs) {
				return throwError(
						   "Função " + function.getName() +
						   " espera no mínimo " + minArgs + " argumentos, recebeu " + argCount,
						   expr.getPosition()
					   );
			}

			if (!function.hasVarargs() && argCount != maxArgs) {
				return throwError(
						   "Função " + function.getName() +
						   " espera exatamente " + maxArgs + " argumentos, recebeu " + argCount,
						   expr.getPosition()
					   );
			}

			try {
				return function.call(current, arguments);
			} catch (RuntimeException e) {
				return throwError(e.getMessage(), expr.getPosition());
			}
		}

		return throwError("Expressão não é chamável: " + callee.type(), expr.getPosition());
	}

	@Override
	public Value visitGetExpr(GetExpr expr) {
		Value object = expr.object.accept(this);
		if (object.isReturn() || object.isThrow()) return object;

		if (object.isTable()) {
			if (!object.asTable().has(expr.name)) {
				return throwError("Propriedade não definida: " + expr.name, expr.getPosition());
			}
			try {
				return object.asTable().get(expr.name);
			} catch (RuntimeException e) {
				return throwError(e.getMessage(), expr.getPosition());
			}
		}

		return throwError("Apenas tabelas têm propriedades", expr.getPosition());
	}

	@Override
	public Value visitSetExpr(SetExpr expr) {
		Value object = expr.object.accept(this);
		if (object.isReturn() || object.isThrow()) return object;

		Value value = expr.value.accept(this);
		if (value.isReturn() || value.isThrow()) return value;

		if (object.isTable()) {
			try {
				object.asTable().set(expr.name, value);
				return value;
			} catch (RuntimeException e) {
				return throwError(e.getMessage(), expr.getPosition());
			}
		}

		return throwError("Apenas tabelas podem ter campos", expr.getPosition());
	}

	@Override
	public Value visitIndexGetExpr(IndexGetExpr expr) {
		Value array = expr.array.accept(this);
		if (array.isReturn() || array.isThrow()) return array;

		Value index = expr.index.accept(this);
		if (index.isReturn() || index.isThrow()) return index;

		if (!array.isArray()) {
			return throwError("Índice só pode ser usado em arrays", expr.getPosition());
		}

		if (!index.isNumber()) {
			return throwError("Índice deve ser número", expr.getPosition());
		}

		int idx = (int) index.asNumber();
		try {
			return array.asArray().get(idx);
		} catch (RuntimeException e) {
			return throwError(e.getMessage(), expr.getPosition());
		}
	}

	@Override
	public Value visitIndexSetExpr(IndexSetExpr expr) {
		Value array = expr.array.accept(this);
		if (array.isReturn() || array.isThrow()) return array;

		Value index = expr.index.accept(this);
		if (index.isReturn() || index.isThrow()) return index;

		Value value = expr.value.accept(this);
		if (value.isReturn() || value.isThrow()) return value;

		if (!array.isArray()) {
			return throwError("Índice só pode ser usado em arrays", expr.getPosition());
		}

		if (!index.isNumber()) {
			return throwError("Índice deve ser número", expr.getPosition());
		}

		int idx = (int) index.asNumber();
		try {
			array.asArray().set(idx, value);
			return value;
		} catch (RuntimeException e) {
			return throwError(e.getMessage(), expr.getPosition());
		}
	}

	@Override
	public Value visitArrayExpr(ArrayExpr expr) {
		List<Value> elements = new ArrayList<>();
		for (Expr element : expr.elements) {
			Value elem = element.accept(this);
			if (elem.isReturn() || elem.isThrow()) return elem;
			elements.add(elem);
		}
		return new ArrayValue(elements);
	}

	@Override
	public Value visitParenthesisExpr(ParenthesisExpr expr) {
		return expr.expression.accept(this);
	}
}