package ast;

import core.Address;
import core.Environment;
import core.Machine;

import java.util.Objects;

public class SampleExpression implements Expression {

	Expression expression;

	public SampleExpression(Expression expression) {
		this.expression = expression;
	}

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		machine.evaluateSample(expression, environment, address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!( o instanceof SampleExpression that )) return false;
		return Objects.equals(expression, that.expression);
	}

	@Override
	public String toString() {
		return "sample " + expression;
	}
}
