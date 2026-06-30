package Instructions;

import ast.Expression;
import core.Environment;
import core.Machine;

public class Evaluate implements Instruction {
	Expression expression;
	Environment environment;
	float address;

	public Evaluate(Expression expression, Environment environment, float address) {
		this.expression = expression;
		this.environment = environment;
		this.address = address;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeEvaluate(this);
	}

	public Expression getExpression() { return expression; }

	public Environment getEnvironment() { return environment; }

	public float getAddress() { return address; }
}
