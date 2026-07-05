package core;

import ast.Expression;

import java.util.List;

public record Closure(
		List<String> params, List<Expression> body, Environment environment) implements Callable {

	@Override
	public void apply(Machine machine, List<Object> args, Address address) {
		Environment newEnvironment = new Environment(environment);
		for (int i = 0; i < params().size(); i++) {
			newEnvironment.add(params().get(i), args.get(i));
		}
		machine.applyClosure(body, newEnvironment, address);
	}
}
