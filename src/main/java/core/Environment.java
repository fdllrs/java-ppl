package core;

import ast.Expression;

import java.util.HashMap;
import java.util.Map;

public class Environment {

	public Environment parent;

	public Map<Expression, Object> variables = new HashMap<>();

	public Environment() {
		parent = null;
	}

	public boolean contains(Expression e) {
		return variables.containsKey(e);
	}

	public void add(Expression e, Object value) {
		variables.put(e, value);
	}
}
