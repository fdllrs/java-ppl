package ast;

import core.Environment;

public class ObserveExpression implements Expression {

	@Override
	public double evaluate(Environment env) {
		return 0;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}
}
