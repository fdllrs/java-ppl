package ast;

import core.Address;
import core.Environment;
import core.Machine;

import java.util.Objects;

public class ObserveExpression implements Expression {

	Expression expression1;
	Expression expression2;

	public ObserveExpression(Expression expression1, Expression expression2) {
		this.expression1 = expression1;
		this.expression2 = expression2;
	}

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {
		machine.evaluateObserve(expression1, expression2, environment, address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression1, expression2);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!( o instanceof ObserveExpression that )) return false;
		return Objects.equals(expression1, that.expression1) && Objects.equals(expression2,
																			   that.expression2);
	}

	@Override
	public String toString() {
		return "observe " + expression1 + " " + expression2;
	}
}
