package ast;

import Instructions.LetK.Binding;
import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;

public record LetExpression(List<Binding> bindings, List<Expression> body) implements Expression {

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		machine.evaluateLet(bindings, body, environment, address);
	}

	@Override
	public String toString() {
		return "let " + bindings + " " + body;
	}
}
