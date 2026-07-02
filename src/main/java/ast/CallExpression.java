package ast;

import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;

public class CallExpression implements Expression {

	private final Expression operator;
	private final List<Expression> operands;

	public CallExpression(Expression operator, List<Expression> operands) {
		this.operator = operator;
		this.operands = operands;
	}

	@Override
	public void evaluate(Environment environment, Address address, Machine machine) {

		machine.evaluateCall(operator, operands, environment, address);
	}

	@Override
	public String toString() {
		return operator + "(" + operands + ")";
	}
}
