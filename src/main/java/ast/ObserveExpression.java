package ast;

import core.Environment;

public class ObserveExpression implements Expression {
	Expression e1;
	Expression e2;

	@Override
	public double evaluate(Environment env) {
		return 0;
	}

	public ObserveExpression(Expression e1, Expression e2) {
		this.e1 = e1; this.e2 = e2;
	}
}
