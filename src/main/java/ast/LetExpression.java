package ast;

import core.Environment;

public class LetExpression implements Expression {
	Expression variable;
	Expression e1;
	Expression e2;

	@Override
	public double evaluate(Environment env) {
		return 0;
	}

	public LetExpression(Expression variable, Expression e1, Expression e2) {
		this.variable = variable; this.e1 = e1; this.e2 = e2;
	}
}
