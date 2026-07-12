package ast;

import core.Address;
import core.Environment;
import core.Machine;
import org.jetbrains.annotations.NotNull;

public record ValueExpression(Object value) implements Expression {

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		machine.evaluateSymbol(this.value);
	}

	@NotNull
	@Override
	public String toString() {
		return value.toString();
	}
}
