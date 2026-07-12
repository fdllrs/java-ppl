package ast;

import core.Address;
import core.Environment;
import core.Machine;
import instructions.LetK.Binding;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record LetExpression(List<Binding> bindings, List<Expression> body) implements Expression {

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		machine.evaluateLet(bindings, body, environment, address);
	}

	@NotNull
	@Override
	public String toString() {
		return "let " + bindings + " " + body;
	}
}
