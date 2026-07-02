package core;

import Instructions.EvaluateK;
import Instructions.Instruction;
import ast.DefnExpression;
import ast.Expression;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class InferenceEngine {
	private final List<Expression> program;
	private final float rng;

	public InferenceEngine(List<Expression> program, float rng) {
		this.program = program;
		this.rng = rng;
	}

	public Machine initializeMachine() {
		assertProgramNotEmpty();
		Environment initialEnvironment = new Environment();
		Expression main = null;
		for (Expression expr : program) {
			if (expr instanceof DefnExpression(
					String name, List<String> params, List<Expression> body
			)) {
				Closure closure = new Closure(params, body, initialEnvironment);
				initialEnvironment.add(name, closure);
			}
			else {
				main = expr;
			}
		}

		Deque<Instruction> controlStack = new ArrayDeque<>();
		controlStack.push(new EvaluateK(main, initialEnvironment, new Address()));

		return new Machine(controlStack, initialEnvironment, new Random((long) ( rng * 100000 )));
	}

	private void assertProgramNotEmpty() {
		if (program.isEmpty()) {
			throw new RuntimeException("Empty program");
		}
	}
}
