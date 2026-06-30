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
		float address = evaluate.getAddress();

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

	}

	public void executeCallK(CallK callK) { }

	public void executeIfK(IfK ifK) { }

	public void executeSampleK(SampleK sampleK) { }

	public void executeObserveK(ObserveK observeK) { }

	public void fork() {

	}
}
