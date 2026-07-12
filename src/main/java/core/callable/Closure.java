package core.callable;

import ast.Expression;
import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;

public record Closure(
		List<String> params, List<Expression> body, Environment environment) implements Callable {

	@Override
	public void apply(Machine machine, List<Object> args, Address address) {
		assertParameterCount(args);

		Environment newEnvironment = new Environment(environment);
		for (int i = 0; i < params().size(); i++) {
			newEnvironment.add(params().get(i), args.get(i));
		}
		machine.applyClosure(body, newEnvironment, address);
	}

	private void assertParameterCount(List<Object> args) {
		if (args.size() != params().size()) {
			throw new IllegalArgumentException("wrong number of arguments");
		}
	}
}
