package ast;

import core.Address;
import core.Environment;
import core.Machine;

import java.util.List;
import java.util.Objects;

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
	public int hashCode() {
		return Objects.hash(operator, operands);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) return true;
		if (!( anObject instanceof CallExpression that )) return false;
		return Objects.equals(operator, that.operator) && Objects.equals(operands, that.operands);
	}

	@Override
	public String toString() {
		return operator + "(" + operands + ")";
	}
}
