package ast;

import core.Environment;

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
	public double evaluate(Environment env) {
		return 0;
	}
}
