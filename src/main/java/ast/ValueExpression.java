package ast;

import core.Address;
import core.Environment;
import core.Machine;

public record ValueExpression(Object value) implements Expression {

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		machine.evaluateSymbol(this.value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
