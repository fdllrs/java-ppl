package ast;

import core.Address;
import core.Environment;
import core.Machine;

public class SymbolExpression implements Expression {
	public String name;

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {

		Object val = environment.lookup(this.name);
		if (val != null) {
			machine.evaluateSymbol(val);
		}
		else {
			throw new RuntimeException("Símbolo no definido: " + this.name);
		}
	}

	public static class Constant { }
}
