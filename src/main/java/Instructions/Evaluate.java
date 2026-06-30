package Instructions;

import ast.Expression;
import core.AddressTag;
import core.Environment;
import core.Machine;

import java.util.Map;

public class Evaluate extends Instruction {
	Expression expression;
	Environment environment;

	public Evaluate(Expression expression,
			Environment environment,
			Map<AddressTag, Integer> addresses) {
		this.expression = expression;
		this.environment = environment;
		this.addresses = addresses;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeEvaluate(this);
	}

	public Expression getExpression() { return expression; }
}
