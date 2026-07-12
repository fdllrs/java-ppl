package ast;

import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;
import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(params, body);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) return true;
		if (!( anObject instanceof FnExpression that )) return false;
		return Objects.equals(params, that.params) && Objects.equals(body, that.body);
	}

	@Override
	public String toString() {
		return String.format("(fn %s %s)", params, body);
	}
}
