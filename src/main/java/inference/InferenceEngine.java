package inference;

import Instructions.EvaluateK;
import Instructions.Instruction;
import ast.DefnExpression;
import ast.Expression;
import core.Address;
import core.Closure;
import core.Environment;
import core.Machine;
import distributions.Distribution;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public abstract class InferenceEngine {
	protected final Random rng;
	private final List<Expression> program;

	public InferenceEngine(List<Expression> program, Random rng) {
		this.program = program;
		this.rng = rng;
	}

	protected Machine initializeMachine() {
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

		return new Machine(controlStack, initialEnvironment, rng);
	}

	private void assertProgramNotEmpty() {
		if (program.isEmpty()) {
			throw new RuntimeException("Empty program");
		}
	}

	public abstract Double run(int iterations);

	public abstract void processSample(Address address, Distribution distribution,
			Machine machine);
	public abstract void processFork();
	public abstract void processObserve(Address address,
			Distribution distribution,
			Object value,
			Machine machine);
	public abstract void processDone(Object returnValue, Machine machine);

	public record MachineResult(double logWeight, Object returnValue) { }
}
