package core;

import ast.Expression;
import core.callable.Callable;
import core.callable.Closure;
import distributions.Distribution;
import instructions.*;
import instructions.LetK.Binding;
import messaging.Done;
import messaging.Message;
import messaging.Observe;
import messaging.Sample;

import java.util.*;

public class Machine {
	private final Environment environment;
	private final Random rng;
	private final Deque<Object> valueStack;
	private final Deque<Instruction> controlStack;
	private double logWeight;
	private Message pendingMessage;

	public Machine(Deque<Instruction> controlStack,
			Deque<Object> valueStack,
			Environment environment,
			Random rng,
			double logWeight) {
		this.controlStack = controlStack;
		this.valueStack = valueStack;
		this.environment = environment;
		this.rng = rng;
		this.logWeight = logWeight;
	}

	public Machine(Deque<Instruction> instructions, Environment environment, Random rng) {
		this.controlStack = instructions;
		this.valueStack = new java.util.ArrayDeque<>();
		this.environment = environment;
		this.rng = rng;

		logWeight = 0;
	}

	public Message resume() {

		while (!controlStack.isEmpty()) {
			Instruction instruction = controlStack.pop();

			instruction.executedBy(this);

			if (pendingMessage != null) {
				return getMessage();
			}
		}

		return new Done(valueStack.pop());
	}

	private Message getMessage() {
		Message msg = pendingMessage;
		pendingMessage = null;
		return msg;
	}

	public Machine fork(Random newRng) {

		return new Machine(new ArrayDeque<>(controlStack),
						   new ArrayDeque<>(valueStack),
						   new Environment(environment),
						   newRng,
						   logWeight);
	}

	public void executeEvaluate(EvaluateK evaluate) {
		Expression expression = evaluate.getExpression();
		Environment env = evaluate.getEnvironment();
		Address address = evaluate.getAddress();

		expression.evaluate(env, address, this);
	}

	public boolean environmentContains(String variableName) {
		return environment.contains(variableName);
	}

	public void addToEnvironment(String variableName, Object value) {
		environment.add(variableName, value);
	}

	public Object lookupInEnvironment(String variableName) {
		return environment.lookup(variableName);
	}

	public void executeLetK(LetK letK) {
		Environment env = letK.getEnvironment();
		int index = letK.getIndex();
		Address address = letK.getAddress();
		List<Expression> body = letK.getBody();

		Binding binding = letK.getBinding(index);
		env.add(binding.variableName(), valueStack.pop());
		if (index + 1 < letK.getBindings().size()) {
			controlStack.push(new LetK(letK.getBindings(), index + 1, body, env, address));
			Expression nextExpression = letK.getBinding(index + 1).valueExpression();

			Address newAddress = address.append(AddressTag.LET);
			controlStack.push(new EvaluateK(nextExpression, env, newAddress));
		}
		else {
			this.pushBody(body, env, address);
		}
	}

	private void pushBody(List<Expression> body, Environment environment, Address address) {

		List<Instruction> instructions = new ArrayList<>();
		for (int i = 0; i < body.size() - 1; i++) {
			Expression expression = body.get(i);

			Address newAddress = address.append(AddressTag.BODY, i);
			instructions.add(new EvaluateK(expression, environment, newAddress));
			instructions.add(new DiscardK());
		}
		Address newAddress = address.append(AddressTag.BODY, body.size());
		instructions.add(new EvaluateK(body.getLast(), environment, newAddress));

		Collections.reverse(instructions);
		for (Instruction instruction : instructions) {
			controlStack.push(instruction);
		}
	}

	public void executeCallK(CallK callK) {
		int paramAmount = callK.getParamAmount();
		Address address = callK.getAddress();
		List<Object> args = new ArrayList<>();
		for (int i = 0; i < paramAmount; i++) {
			args.add(valueStack.pop());
		}
		Collections.reverse(args);

		Object f = valueStack.pop();
		if (f instanceof Callable callable) {
			callable.apply(this, args, address);
		}
		else {
			throw new RuntimeException("Object is not callable: " + f);
		}
	}

	public void executeIfK(IfK ifK) {
		Address address = ifK.getAddress();

		Expression branch;
		AddressTag tag;

		boolean testExpression = popBoolean();

		if (testExpression) {
			branch = ifK.getThenExpression();
			tag = AddressTag.THEN;
		}
		else {
			branch = ifK.getElseExpression();
			tag = AddressTag.ELSE;
		}
		controlStack.push(new EvaluateK(branch, ifK.getEnvironment(), address.append(tag)));
	}

	private boolean popBoolean() {
		boolean testExpression;
		try {
			testExpression = (boolean) valueStack.pop();
		} catch (ClassCastException e) {
			throw new RuntimeException("Test expression must evaluate to a boolean");
		}
		return testExpression;
	}

	public void executeSampleK(SampleK sampleK) {
		Distribution distribution = (Distribution) valueStack.pop();
		pendingMessage = new Sample(sampleK.getAddress(), distribution);
	}

	public void executeObserveK(ObserveK observeK) {
		Object value = valueStack.pop();
		Distribution distribution = (Distribution) valueStack.pop();
		pendingMessage = new Observe(observeK.getAddress(), distribution, value);
	}

	public void executeDiscard() {
		valueStack.pop();
	}

	public void evaluateSymbol(Object value) {
		pushResult(value);
	}

	public void pushResult(Object result) {
		valueStack.push(result);
	}

	public void evaluateIf(Expression testExpression,
			Expression thenExpression,
			Expression elseExpression,
			Environment environment,
			Address address) {
		controlStack.push(new IfK(testExpression,
								  thenExpression,
								  elseExpression,
								  environment,
								  address));

		controlStack.push(new EvaluateK(testExpression,
										environment,
										address.append(AddressTag.TEST)));
	}

	public void evaluateObserve(Expression expression1,
			Expression expression2,
			Environment environment,
			Address address) {

		controlStack.push(new ObserveK(address));

		controlStack.push(new EvaluateK(expression2,
										environment,
										address.append(AddressTag.VALUE)));

		controlStack.push(new EvaluateK(expression1,
										environment,
										address.append(AddressTag.DISTRIBUTION)));
	}

	public void evaluateSample(Expression expression, Environment environment, Address address) {
		controlStack.push(new SampleK(address));

		controlStack.push(new EvaluateK(expression,
										environment,
										address.append(AddressTag.DISTRIBUTION)));
	}

	public void evaluateFn(List<String> params, List<Expression> body, Environment environment) {
		pushResult(new Closure(params, body, environment));
	}

	public void evaluateCall(Expression operator,
			List<Expression> operands,
			Environment environment,
			Address address) {

		controlStack.push(new CallK(operands.size(), address));

		for (int i = operands.size() - 1; i >= 0; i--) {
			controlStack.push(new EvaluateK(operands.get(i), environment, address.append(i)));
		}
		controlStack.push(new EvaluateK(operator, environment, address.append(AddressTag.FN)));
	}

	public void evaluateLet(List<Binding> binds,
			List<Expression> body,
			Environment environment,
			Address address) {
		if (!binds.isEmpty()) {
			controlStack.push(new LetK(binds, 0, body, environment, address));
			Expression expressionToEvaluate = binds.getFirst().valueExpression();

			controlStack.push(new EvaluateK(expressionToEvaluate,
											environment,
											address.append(AddressTag.LET)));
		}
		else {
			this.pushBody(body, environment, address);
		}
	}

	public Random getRng() { return rng; }

	public void send(Object sample) {
		pushResult(sample);
	}

	public void addToLogWeight(double weight) {
		logWeight += weight;
	}

	public double getLogWeight() { return logWeight; }

	public void applyClosure(List<Expression> body, Environment newEnvironment, Address address) {
		this.pushBody(body, newEnvironment, address);
	}
}
