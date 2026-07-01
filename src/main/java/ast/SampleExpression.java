package ast;

import core.Address;
import core.Environment;
import core.Machine;

public class SampleExpression implements Expression {

	Expression expression;

	public SampleExpression(Expression expression) {
		this.expression = expression;
	}

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {

		machine.evaluateSample(expression, environment, address);
	}
}
