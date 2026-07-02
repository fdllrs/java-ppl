package inference;

import ast.Expression;
import core.Machine;
import messaging.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LikelihoodWeighting extends InferenceEngine {

	public LikelihoodWeighting(List<Expression> program, Random rng) {
		super(program, rng);
	}

	@Override
	public Double run(int iterations) {
		List<Double> values = new ArrayList<>();
		List<Double> logWeights = new ArrayList<>();

		for (int i = 0; i < iterations; i++) {
			runIteration(values, logWeights);
		}

		return calculateWeightedMean(logWeights, values);
	}

	private void runIteration(List<Double> values, List<Double> logWeights) {
		MachineResult result = executeLikelihoodWeighting();
		values.add((Double) result.returnValue());
		logWeights.add(result.logWeight());
	}

	private static double calculateWeightedMean(List<Double> logWeights, List<Double> values) {
		List<Double> softMax = softmax(logWeights);
		double mean = 0;
		for (int i = 0; i < values.size(); i++) {
			mean += values.get(i) * softMax.get(i);
		}
		return mean;
	}

	private MachineResult executeLikelihoodWeighting() {
		Machine machine = initializeMachine();
		while (true) {
			Message message = machine.resume();
			switch (message) {
				case Sample(_, var distribution) -> {
					Object sampleVal = distribution.sample(machine.getRng());
					machine.send(sampleVal);
				}
				case Observe(_, var distribution, var observation) -> {
					machine.addToLogWeight(distribution.logProb(observation));
					machine.send(observation);
				}
				case Done(var returnValue) -> {
					return new MachineResult(machine.getLogWeight(), returnValue);
				}
				case Fork() ->
						throw new RuntimeException("Should not fork in likelihood weighting");
			}
		}
	}
}
