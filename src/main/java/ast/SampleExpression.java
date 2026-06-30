package ast;

import core.Environment;

public class SampleExpression implements Expression {
	Expression e;

	public SampleExpression(Expression e) {
		this.e = e;
	}

	@Override
	public double evaluate(Environment env) {
		return 0;
	}
}
