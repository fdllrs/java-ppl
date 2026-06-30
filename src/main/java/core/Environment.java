package core;

import ast.Expression;

import java.util.HashMap;
import java.util.Map;

public class Environment {

	public Environment parent;

	public Map<Expression, Double> variables = new HashMap<>();

	public Environment() {
		parent = null;
	}

	public boolean contains(Expression e) {
		return variables.containsKey(e);
	}
}
