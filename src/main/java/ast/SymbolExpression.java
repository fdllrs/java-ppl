package ast;

import core.Address;
import core.Environment;
import core.Machine;
import core.callable.PrimitiveFunction;
import org.jetbrains.annotations.NotNull;

public record SymbolExpression(String name) implements Expression {

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		PrimitiveFunction primitive = PrimitiveFunction.fromSymbol(this.name);
		Object val;
		if (primitive != null) { val = primitive; }
		else { val = environment.lookup(this.name); }

		machine.evaluateSymbol(val);
	}

	@NotNull
	@Override
	public String toString() {
		return String.valueOf(this.name);
	}
}
