package ast;

import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;

public class FnExpression implements Expression {

	private final List<String> params;
	private final List<Expression> body;

	public FnExpression(List<String> params, List<Expression> body) {
		this.params = params;
		this.body = body;
	}

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		machine.evaluateFn(params, body, environment);
	}
}
