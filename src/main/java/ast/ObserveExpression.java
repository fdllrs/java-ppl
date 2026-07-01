package ast;

import core.Address;
import core.Environment;
import core.Machine;

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
}
