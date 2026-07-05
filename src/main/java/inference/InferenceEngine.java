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
	protected final List<Expression> program;

	public InferenceEngine(List<Expression> program, Random rng) {
		this.program = program;
		this.rng = rng;
	}

	public static ArrayList<Double> softmax(ArrayList<Double> logWeights) {
		ArrayList<Double> probabilities = new ArrayList<>();

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
				main = expr;
			}
		}
		return new EvaluateK(main, environment, new Address());
	}

	public abstract ArrayList<Double> run(int iterations);

	public record MachineResult(double logWeight, Object returnValue) { }
}
