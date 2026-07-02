package ast;

import core.Address;
import core.Environment;
import core.Machine;

public class IfExpression implements Expression {
	Expression testExpression;
	Expression thenExpression;
	Expression elseExpression;

	public IfExpression(Expression testExpression,
			Expression thenExpression,
			Expression elseExpression) {
		this.testExpression = testExpression;
		this.thenExpression = thenExpression;
		this.elseExpression = elseExpression;
	}

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {

		machine.evaluateIf(testExpression, thenExpression, elseExpression, environment, address);
	}

	@Override
	public String toString() {
		return "if " + testExpression + " " + thenExpression + " else " + elseExpression;
	}
}
