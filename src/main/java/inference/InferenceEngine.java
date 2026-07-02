package inference;

import Instructions.EvaluateK;
import Instructions.Instruction;
import ast.DefnExpression;
import ast.Expression;
import core.Address;
import core.Closure;
import core.Environment;
import core.Machine;

import java.util.*;

public abstract class InferenceEngine {
	protected final Random rng;
	private final List<Expression> program;

	public InferenceEngine(List<Expression> program, Random rng) {
		this.program = program;
		this.rng = rng;
	}

	public static List<Double> softmax(List<Double> logWeights) {
		List<Double> probabilities = new ArrayList<>();

		double max = logWeights.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

		double sum = 0.0;
		for (int i = 0; i < logWeights.size(); i++) {
			probabilities.add(i, Math.exp(logWeights.get(i) - max));
			sum += probabilities.get(i);
		}

		for (int i = 0; i < probabilities.size(); i++) {
			probabilities.set(i, probabilities.get(i) / sum);
		}

		return probabilities;
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

	public record MachineResult(double logWeight, Object returnValue) { }
}
