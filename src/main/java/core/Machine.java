package core;

import Instructions.*;
import ast.Expression;
import ast.SymbolExpression;

import java.util.Deque;
import java.util.List;
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
		Address addresses = evaluate.getAddress();

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
		Address address = letK.getAddress();
		Object body = letK.getBody();

		Expression bind = (Expression) binds.get(2 * index);

		env.add(bind, valueStack.pop());

		if (2 * ( index + 1 ) < binds.size()) {
			controlStack.add(new LetK(binds, index + 1, body, env, address));
			Expression expressionToEvaluate = (Expression) binds.get(2 * ( index + 1 ));

			address.append(AddressTag.LET, 2 * ( index + 1 ));
			controlStack.add(new Evaluate(expressionToEvaluate, env, address));
		}
		else {
			this.pushBody();
		}
	}

	private void pushBody() { throw new RuntimeException("not yet implemented"); }

	public void executeCallK(CallK callK) { }

	public void executeIfK(IfK ifK) {
		Expression testExpression = ifK.getTestExpression();
		Expression thenExpression = ifK.getThenExpression();
		Expression elseExpression = ifK.getElseExpression();

		Environment env = ifK.getEnvironment();
		Address address = ifK.getAddress();

		Expression branch;
		AddressTag tag;
		if ((boolean) valueStack.pop()) {
			branch = thenExpression;
			tag = AddressTag.THEN;
		}
		else {
			branch = elseExpression;
			tag = AddressTag.ELSE;
		}
		Address newAddress = address.append(tag, 0);
		controlStack.add(new Evaluate(branch, env, newAddress));
	}

	public void executeSampleK(SampleK sampleK) { }

	public void executeObserveK(ObserveK observeK) { }

	public void fork() {

	}

	public void executeDiscard(Discard discard) {
		valueStack.pop();
	}
}
