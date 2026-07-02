package inference;

import ast.Expression;
import core.Address;
import core.Machine;
import distributions.Distribution;
import messaging.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LikelihoodWeighting extends InferenceEngine {

	private boolean inferenceDone = false;

	private MachineResult result;

	public LikelihoodWeighting(List<Expression> program, Random rng) {
		super(program, rng);
	}

	@Override
	public Double run(int iterations) {
		List<Double> values = new ArrayList<>();
		List<Double> logWeights = new ArrayList<>();

		for (int i = 0; i < iterations; i++) {
			MachineResult result = executeLikelihoodWeighting();
			values.add((Double) result.returnValue());
			logWeights.add(result.logWeight());
		}

		List<Double> softMax = softmax(logWeights);
		double mean = 0;
		for (int i = 0; i < values.size(); i++) {
			mean += values.get(i) * softMax.get(i);
		}

		return mean;
	}

	private MachineResult executeLikelihoodWeighting() {
		inferenceDone = false;
		result = null;
		Machine machine = initializeMachine();
		while (true) {
			Message message = machine.resume();
			message.processMessage(this);

			if (inferenceDone) {
				return result;
			}
		}
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

	@Override
	public void processSample(Address address, Distribution distribution, Machine machine) {
		machine.send(distribution.sample(machine.getRng()));
	}

	@Override
	public void processFork() { }

	@Override
	public void processObserve(Address address,
			Distribution distribution,
			Object observedValue,
			Machine machine) {

		machine.addToLogWeight(distribution.logProb(observedValue));
		machine.send(observedValue);
	}

	@Override
	public void processDone(Object returnValue, Machine machine) {
		inferenceDone = true;
		result = new MachineResult(machine.getLogWeight(), returnValue);
	}
}
