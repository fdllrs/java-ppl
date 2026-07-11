package inference;

import ast.Expression;
import core.Machine;
import distributions.Distribution;
import messaging.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LikelihoodWeighting <T> extends InferenceEngine<T> {

	private final int iterations;

	public LikelihoodWeighting(List<Expression> program, Random rng, int iterations) {
		super(program, rng);

		this.iterations = iterations;
	}

	@Override
	public Posterior<T> run() {
		ArrayList<T> values = new ArrayList<>();
		ArrayList<Double> logWeights = new ArrayList<>();

		for (int i = 0; i < iterations; i++) {
			runIteration(values, logWeights);
		}

		ArrayList<Double> weights = softmax(logWeights);
		return new Posterior<>(values, weights);
	}

	@SuppressWarnings("unchecked")
	private void runIteration(List<T> values, List<Double> logWeights) {
		MachineResult result = executeLikelihoodWeighting();
		values.add((T) result.returnValue());
		logWeights.add(result.logWeight());
	}

	private MachineResult executeLikelihoodWeighting() {
		Machine machine = initializeMachine();
		while (true) {
			Message message = machine.resume();
			switch (message) {
				case Sample(_, Distribution distribution) -> {
					Object sampleVal = distribution.sample(machine.getRng());
					machine.send(sampleVal);
				}
				case Observe(_, Distribution distribution, Object observation) -> {
					machine.addToLogWeight(distribution.logProb(observation));
					machine.send(observation);
				}
				case Done(Object returnValue) -> {
					return new MachineResult(machine.getLogWeight(), returnValue);
				}
				case Fork() ->
						throw new RuntimeException("Should not fork in likelihood weighting");
			}
		}
	}
}
