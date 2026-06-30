package core;

import Instructions.*;
import ast.Expression;
import ast.ObserveExpression;
import ast.SampleExpression;
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
			if (env.contains(e)) valueStack.push(e);

			else if (e.isPrimitive()) valueStack.push(e);

			else throw new RuntimeException("Cannot evaluate expression");
		}
		else if (!( e instanceof List<?> )) {
			valueStack.push(e);
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
			controlStack.push(new LetK(binds, index + 1, body, env, address));
			Expression expressionToEvaluate = (Expression) binds.get(2 * ( index + 1 ) + 1);

			Address newAddress = address.append(AddressTag.LET, 2 * ( index + 1 ));
			controlStack.push(new Evaluate(expressionToEvaluate, env, newAddress));
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
		controlStack.push(new Evaluate(branch, env, newAddress));
	}

	public SampleExpression executeSampleK(SampleK sampleK) {
		Address address = sampleK.getAddress();
		Object distribution = valueStack.pop();

		return new SampleExpression(address, distribution);
	}

	public ObserveExpression executeObserveK(ObserveK observeK) {

		Address address = observeK.getAddress();
		Object distribution = valueStack.pop();

		return new ObserveExpression(address, distribution);
	}

	public void fork() {

	}

	public void executeDiscard(Discard discard) {
		valueStack.pop();
	}
}
