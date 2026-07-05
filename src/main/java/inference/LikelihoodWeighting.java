package inference;

import ast.Expression;
import core.Machine;
import distributions.Distribution;
import messaging.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LikelihoodWeighting extends InferenceEngine {

	public LikelihoodWeighting(List<Expression> program, Random rng) {
		super(program, rng);
	}

	@Override
	public ArrayList<Double> run(int iterations) {
		ArrayList<Double> values = new ArrayList<>();
		ArrayList<Double> logWeights = new ArrayList<>();

		for (int i = 0; i < iterations; i++) {
			runIteration(values, logWeights);
		}

		return weightValues(logWeights, values);
	}

	private void runIteration(List<Double> values, List<Double> logWeights) {
		MachineResult result = executeLikelihoodWeighting();
		values.add(( (Number) result.returnValue() ).doubleValue());
		logWeights.add(result.logWeight());
	}

	public static ArrayList<Double> weightValues(ArrayList<Double> logWeights,
			ArrayList<Double> values) {

		ArrayList<Double> softMax = softmax(logWeights);
		for (int i = 0; i < values.size(); i++) {
			values.set(i, values.get(i) * softMax.get(i));
		}

		return values;

		//		double mean = 0;
		//		for (int i = 0; i < values.size(); i++) {
		//			mean += values.get(i) * softMax.get(i);
		//		}
		//		return mean;
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
