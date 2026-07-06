package inference;

import ast.Expression;
import core.Machine;
import distributions.Distribution;
import messaging.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LikelihoodWeighting extends InferenceEngine {

	private final int iterations;
	private ArrayList<Double> weights;

	public LikelihoodWeighting(List<Expression> program, Random rng, int iterations) {
		super(program, rng);

		this.iterations = iterations;
	}

	public static double calculateWeightedMean(List<Double> samples, List<Double> weights) {
		double sum = 0.0;
		for (int i = 0; i < samples.size(); i++) {
			sum += samples.get(i) * weights.get(i);
		}
		return sum;
	}

	@Override
	public ArrayList<Double> run() {
		ArrayList<Double> values = new ArrayList<>();
		ArrayList<Double> logWeights = new ArrayList<>();

		for (int i = 0; i < iterations; i++) {
			runIteration(values, logWeights);
		}

		this.weights = softmax(logWeights);
		return values;
	}

	private void runIteration(List<Double> values, List<Double> logWeights) {
		MachineResult result = executeLikelihoodWeighting();
		values.add(( (Number) result.returnValue() ).doubleValue());
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

	public ArrayList<Double> getWeights() {
		return weights;
	}
}
