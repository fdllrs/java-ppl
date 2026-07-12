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
	public boolean equals(Object anObject) {
		if (this == anObject) return true;
		if (!( anObject instanceof SampleExpression sampleExpression )) return false;
		return Objects.equals(expression, sampleExpression.expression);
	}

	@Override
	public String toString() {
		return "sample " + expression;
	}
}
