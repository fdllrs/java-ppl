package Instructions;

import ast.Expression;
import core.Address;
import core.Environment;
import core.Machine;

public class IfK extends Instruction {
	Expression testExpression;
	Expression thenExpression;
	Expression elseExpression;

	public IfK(Expression testExpression,
			Expression thenExpression,
			Expression elseExpression,
			Environment environment,
			Address addresses) {
		this.testExpression = testExpression;
		this.thenExpression = thenExpression;
		this.elseExpression = elseExpression;
		this.environment = environment;
		this.address = addresses;
	}

	public Expression getTestExpression() { return testExpression; }

	public Expression getThenExpression() { return thenExpression; }

	public Expression getElseExpression() { return elseExpression; }

	@Override
	public void executedBy(Machine machine) {
		machine.executeIfK(this);
	}
}
