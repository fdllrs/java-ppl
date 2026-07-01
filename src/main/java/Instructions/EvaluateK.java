package Instructions;

import ast.Expression;
import core.Address;
import core.Environment;
import core.Machine;

public class EvaluateK extends Instruction {
	Expression expression;
	Environment environment;

	public EvaluateK(Expression expression, Environment environment, Address addresses) {
		this.expression = expression;
		this.environment = environment;
		this.address = addresses;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeEvaluate(this);
	}

	public Expression getExpression() { return expression; }
}
