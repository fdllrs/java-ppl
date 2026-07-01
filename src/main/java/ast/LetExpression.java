package ast;

import core.Address;
import core.Environment;
import core.Machine;

public class LetExpression implements Expression {
	Expression variable;
	Expression e1;
	Expression e2;

	public LetExpression(Expression variable, Expression e1, Expression e2) {
		this.variable = variable;
		this.e1 = e1;
		this.e2 = e2;
	}

	@Override
	public void evaluate(Environment env, Address address, Machine machine) {

	}
}
