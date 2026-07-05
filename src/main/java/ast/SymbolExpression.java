package ast;

import core.Address;
import core.Environment;
import core.Machine;
import core.PrimitiveFunction;

public record SymbolExpression(String name) implements Expression {

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		Object val = environment.lookup(this.name);
		if (val == null && isPrimitive(this.name)) val = new PrimitiveFunction(this.name);

		if (val != null) {
			machine.evaluateSymbol(val);
		}
		else {
			throw new RuntimeException("undefined Symbol: " + this.name);
		}
	}

	private boolean isPrimitive(String name) {
		return name.equals("+") || name.equals("-") || name.equals("*") || name.equals("/") ||
			   name.equals(">") || name.equals("<") || name.equals("=") || name.equals("==") ||
			   name.equals("normal") || name.equals("bernoulli");
	}

	@Override
	public String toString() {
		return String.valueOf(this.name);
	}
}
