package ast;

import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;

public record DefnExpression(String name, List<String> params, List<Expression> body) implements
		Expression {

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		throw new UnsupportedOperationException("defn cannot be evaluated directly");
	}

	@Override
	public String toString() {
		return String.format("(defn %s %s %s)", name, params, body);
	}
}
