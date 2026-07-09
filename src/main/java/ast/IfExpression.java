package ast;

import core.Address;
import core.Environment;
import core.Machine;

import java.util.Objects;

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
	public int hashCode() {
		return Objects.hash(testExpression, thenExpression, elseExpression);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!( o instanceof IfExpression that )) return false;
		return Objects.equals(testExpression, that.testExpression) && Objects.equals(thenExpression,
																					 that.thenExpression) &&
			   Objects.equals(elseExpression, that.elseExpression);
	}

	@Override
	public String toString() {
		return "if " + testExpression + " " + thenExpression + " else " + elseExpression;
	}
}
