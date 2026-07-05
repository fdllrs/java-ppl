package ast;

import core.Address;
import core.Environment;
import core.Machine;
import core.callable.PrimitiveFunction;

public record SymbolExpression(String name) implements Expression {

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		Object val = environment.lookup(this.name);
		PrimitiveFunction primitive = PrimitiveFunction.fromSymbol(this.name);
		if (val == null && primitive != null) val = primitive;

		if (val != null) {
			machine.evaluateSymbol(val);
		}
		else {
			throw new RuntimeException("undefined Symbol: " + this.name);
		}
	}

	@Override
	public String toString() {
		return String.valueOf(this.name);
	}
}
