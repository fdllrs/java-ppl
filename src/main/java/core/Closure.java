package core;

import ast.Expression;

import java.util.List;

public class Closure {
	private final List<String> params;
	private final List<Expression> body;
	private final Environment environment;

	public Closure(List<String> params, List<Expression> body, Environment environment) {
		this.params = params;
		this.body = body;
		this.environment = environment;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public List<String> getParams() { return params; }
}
