package inference;

import ast.DefnExpression;
import ast.Expression;
import core.Address;
import core.Environment;
import core.Machine;
import core.callable.Closure;
import instructions.EvaluateK;
import instructions.Instruction;

import java.util.*;

public abstract class InferenceEngine <T extends Number> {
	protected final Random rng;
	protected final List<Expression> program;

	public InferenceEngine(List<Expression> program, Random rng) {
		this.program = program;
		this.rng = rng;
	}

	public static ArrayList<Double> softmax(ArrayList<Double> logWeights) {
		double max = logWeights.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

		if (Double.isInfinite(max) && max < 0) {
			throw new IllegalStateException(
					"All log-weights are -Infinity: the model has zero probability mass " +
					"on every trace (degenerate particle collapse).");
		}

		ArrayList<Double> probabilities = new ArrayList<>(logWeights.size());
		double sum = 0.0;
		for (double lw : logWeights) {
			double p = Math.exp(lw - max);
			probabilities.add(p);
			sum += p;
		}

		for (int i = 0; i < probabilities.size(); i++) {
			probabilities.set(i, probabilities.get(i) / sum);
		}

		return probabilities;
	}

	protected Machine initializeMachine() {
		return initializeMachineWithRNG(this.rng);
	}

	protected Machine initializeMachineWithRNG(Random rng) {
		assertProgramNotEmpty();
		Environment initialEnvironment = new Environment();
		Deque<Instruction> controlStack = new ArrayDeque<>();

		EvaluateK evaluation = buildMainEvaluation(initialEnvironment);
		controlStack.push(evaluation);
		return new Machine(controlStack, initialEnvironment, rng);
	}

	private void assertProgramNotEmpty() {
		if (program.isEmpty()) {
			throw new RuntimeException("Empty program");
		}
	}

	protected EvaluateK buildMainEvaluation(Environment environment) {
		Expression main = null;
		for (Expression expr : program) {
			if (expr instanceof DefnExpression(
					String name, List<String> params, List<Expression> body
			)) {
				Closure closure = new Closure(params, body, environment);
				environment.add(name, closure);
			}
			else {
				if (main != null) {
					throw new RuntimeException("Program has multiple top-level expressions. " +
											   "Only one non-defn expression is allowed as the " +
											   "program entry point.");
				}
				main = expr;
			}
		}
		if (main == null) {
			throw new RuntimeException("Program has no top-level expression to evaluate.");
		}
		return new EvaluateK(main, environment, new Address());
	}

	public abstract Posterior<T> run();

	public record MachineResult(double logWeight, Object returnValue) { }
}
