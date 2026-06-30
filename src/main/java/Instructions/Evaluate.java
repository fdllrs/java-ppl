package Instructions;

import ast.Expression;
import core.Environment;
import core.Machine;

import java.util.Map;

public class Evaluate extends Instruction {
	Expression expression;
	Environment environment;
	Map<String, Integer> address;

	public Evaluate(Expression expression, Environment environment, Map<String, Integer> address) {
		this.expression = expression;
		this.environment = environment;
		this.address = address;
	}

	@Override
	public void executedBy(Machine machine) {
		machine.executeEvaluate(this);
	}

	public Expression getExpression() { return expression; }

	public Map<String, Integer> getAddress() { return address; }
}
