package core;

import Instructions.*;
import ast.Expression;
import ast.SymbolExpression;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Machine {
	Deque<Instruction> controlStack;
	Deque<Object> valueStack;
	double logWeight;
	Random rng;
	Environment environment;

	public Machine(Deque<Instruction> instructions) {
		controlStack = instructions;
		valueStack = new java.util.ArrayDeque<>();
		rng = new Random();
		environment = new Environment();

		logWeight = 0;
	}

	public void resume() {

		while (!controlStack.isEmpty()) {
			Instruction instruction = controlStack.pop();

			instruction.executedBy(this);
		}
	}

	public void executeEvaluate(Evaluate evaluate) {
		Expression e = evaluate.getExpression();
		Environment env = evaluate.getEnvironment();
		Map<String, Integer> address = evaluate.getAddress();

		if (e instanceof SymbolExpression) {
			if (env.contains(e)) valueStack.add(e);

			else if (e.isPrimitive()) valueStack.add(e);

			else throw new RuntimeException("Cannot evaluate expression");
		}
		else if (!( e instanceof List<?> )) {
			valueStack.add(e);
		}
		else {
			throw new RuntimeException("not yet implemented");
		}
	}

	public void executeLetK(LetK letK) {
		Environment env = letK.getEnvironment();
		List<Object> binds = letK.getBinds();
		int index = letK.getIndex();
		Map<String, Integer> address = letK.getAddress();
		Object body = letK.getBody();

		Expression bind = (Expression) binds.get(2 * index);

		env.add(bind, valueStack.pop());

		if (2 * ( index + 1 ) < binds.size()) {
			controlStack.add(new LetK(binds, index + 1, body, env, address));
			Expression expressionToEvaluate = (Expression) binds.get(2 * ( index + 1 ));
			address.put("let", 2 * ( index + 1 ));
			controlStack.add(new Evaluate(expressionToEvaluate, env, address));
		}
		else {
			this.pushBody();
		}
	}

	private void pushBody() { throw new RuntimeException("not yet implemented"); }

	public void executeCallK(CallK callK) { }

	public void executeIfK(IfK ifK) { }

	public void executeSampleK(SampleK sampleK) { }

	public void executeObserveK(ObserveK observeK) { }

	public void fork() {

	}
}
