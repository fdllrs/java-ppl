package core;

import Instructions.*;
import ast.Expression;
import messaging.Message;
import messaging.Observe;
import messaging.Return;
import messaging.Sample;

import java.util.*;

public class Machine {
	Deque<Instruction> controlStack;
	Deque<Object> valueStack;
	double logWeight;
	Random rng;
	Environment environment;

	private Message pendingMessage;

	public Machine(Deque<Instruction> instructions) {
		controlStack = instructions;
		valueStack = new java.util.ArrayDeque<>();
		rng = new Random();
		environment = new Environment();

		logWeight = 0;
	}

	public Message resume() {

		while (!controlStack.isEmpty()) {
			Instruction instruction = controlStack.pop();

			instruction.executedBy(this);

			if (pendingMessage != null) {
				Message msg = pendingMessage;
				pendingMessage = null;
				return msg;
			}
		}

		return new Return(valueStack.pop());
	}

	public void executeEvaluate(EvaluateK evaluate) {
		Expression expression = evaluate.getExpression();
		Environment env = evaluate.getEnvironment();
		Address address = evaluate.getAddress();

		expression.evaluate(env, address, this);
	}

	public void executeLetK(LetK letK) {
		Environment env = letK.getEnvironment();
		List<Object> binds = letK.getBinds();
		int index = letK.getIndex();
		Address address = letK.getAddress();
		List<Expression> body = letK.getBody();

		String bind = (String) binds.get(2 * index);

		env.add(bind, valueStack.pop());

		if (2 * ( index + 1 ) < binds.size()) {
			controlStack.push(new LetK(binds, index + 1, body, env, address));
			Expression expressionToEvaluate = (Expression) binds.get(2 * ( index + 1 ) + 1);

			Address newAddress = address.append(AddressTag.LET, 2 * ( index + 1 ));
			controlStack.push(new EvaluateK(expressionToEvaluate, env, newAddress));
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
		if (f instanceof Closure(
				List<String> functionParams, List<Expression> body, Environment environment1
		)) {
			Environment newEnvironment = new Environment(environment1);
			for (int i = 0; i < paramAmount; i++) {
				newEnvironment.add(functionParams.get(i), args.get(i));
			}
			pushBody(body, newEnvironment, address);
		}
		else {
			Object result = applyPrimitive(f, args);
			valueStack.push(result);
		}
	}

	private Object applyPrimitive(Object f, List<Object> args) {
		throw new RuntimeException("not yet implemented");
	}

	public void executeIfK(IfK ifK) {
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
		controlStack.push(new EvaluateK(branch, env, newAddress));
	}

	public void executeSampleK(SampleK sampleK) {
		Address address = sampleK.getAddress();
		Object distribution = valueStack.pop();

		pendingMessage = new Sample(address, distribution, this);
	}

	public void executeObserveK(ObserveK observeK) {

		Address address = observeK.getAddress();
		Object distribution = valueStack.pop();

		pendingMessage = new Observe(address, distribution, this);
	}

	public void fork() {

	}

	public void executeDiscard() {
		valueStack.pop();
	}

	public void evaluateSymbol(Object value) {
		valueStack.push(value);
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

		Address newAddress = address.append(AddressTag.TEST, 0);
		controlStack.push(new EvaluateK(testExpression, environment, newAddress));
	}

	public void evaluateObserve(Expression expression1,
			Expression expression2,
			Environment environment,
			Address address) {

		controlStack.push(new ObserveK(address));

		Address newAddressValue = address.append(AddressTag.VALUE, 0);
		controlStack.push(new EvaluateK(expression2, environment, newAddressValue));

		Address newAddressDistribution = address.append(AddressTag.DISTRIBUTION, 0);
		controlStack.push(new EvaluateK(expression1, environment, newAddressDistribution));
	}

	public void evaluateSample(Expression expression, Environment environment, Address address) {
		controlStack.push(new SampleK(address));

		Address newAddressDistribution = address.append(AddressTag.DISTRIBUTION, 0);
		controlStack.push(new EvaluateK(expression, environment, newAddressDistribution));
	}

	public void evaluateFn(List<String> params, List<Expression> body, Environment environment) {
		valueStack.push(new Closure(params, body, environment));
	}

	public void evaluateCall(Expression operator,
			List<Expression> operands,
			Environment environment,
			Address address) {

		controlStack.push(new CallK(operands.size(), address));

		for (int i = operands.size() - 1; i >= 0; i--) {
			Address argAddress = address.append(i);
			controlStack.push(new EvaluateK(operands.get(i), environment, argAddress));
		}
		Address fnAddr = address.append(AddressTag.FN, 0);
		controlStack.push(new EvaluateK(operator, environment, fnAddr));
	}
}
