package inference;

import ast.Expression;
import core.Machine;
import distributions.Distribution;
import messaging.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LikelihoodWeighting <T> extends InferenceEngine<T> {

	private final int iterations;

	public LikelihoodWeighting(List<Expression> program, Random rng, int iterations) {
		super(program, rng);

		this.iterations = iterations;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Posterior<T> run() {

		List<Random> iterationRngs = initializeRandomGenerators();

		List<MachineResult> results = iterationRngs.parallelStream()
												   .map(this::executeLikelihoodWeighting)
												   .toList();

		ArrayList<T> values = new ArrayList<>(iterations);
		ArrayList<Double> logWeights = new ArrayList<>(iterations);

		for (MachineResult result : results) {
			values.add((T) result.returnValue());
			logWeights.add(result.logWeight());
		}

		ArrayList<Double> weights = softmax(logWeights);
		return new Posterior<>(values, weights);
	}

	private @NotNull List<Random> initializeRandomGenerators() {
		List<Random> iterationRngs = new ArrayList<>(iterations);
		for (int i = 0; i < iterations; i++) {
			iterationRngs.add(new Random(rng.nextLong()));
		}
		return iterationRngs;
	}

	private MachineResult executeLikelihoodWeighting(Random rng) {
		Machine machine = initializeMachineWithRNG(rng);
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
